package com.mathMachine;

import jdk.jfr.DataAmount;
import lombok.Data;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

@Data
public class CalculateUtil {

	private List<String> variable;

	private List<String> variableVal;

	private LinkedList<String> formula;

	private String ans;
	
	private Integer scale;

	private int recursionCount;

	public String getAns() {
		return ans;
	}
	
	public void setScale(Integer scale) {
		this.scale = scale;
	}
	
	private void setAns(BigDecimal ans) {
		this.ans = ans.toString();
	}

	public List<String> getVariable() {
		return variable;
	}

	public List<String> getVariableVal() {
		return variableVal;
	}

	public List<String> getFormula() {
		return formula;
	}

	private void setVariable(List<String> formulaList) {
		this.variable = new ArrayList<String>();

		StringBuilder variableName = new StringBuilder();
		int length = formulaList.size();
		String regex = "[a-zA-Z]";

		for (int i = 0; i < length; i++) {
			if (formulaList.get(i).matches(regex)) {
				variableName.append(formulaList.get(i));
				if (i == length - 1) {
					if (!this.variable.contains(variableName.toString())) {
						this.variable.add(variableName.toString());
					}
					variableName = new StringBuilder();
				}
			} else if (!variableName.isEmpty()) {
				if (!this.variable.contains(variableName.toString())) {
					this.variable.add(variableName.toString());
				}
				variableName = new StringBuilder();
			}
		}
		this.variable.remove("e");
		this.variable.remove("E");
		
	}

	private LinkedList<String> setPartFormula(String formula) {
		List<String> formulaList = Arrays.asList(formula.split(""));
		LinkedList<String> partFormula = new LinkedList<String>();

		String function = "";

		boolean complex = false;

		int countLeftParentheses = 0;
		int countRightParentheses = 0;

		for (int i = 0; i < formulaList.size(); i++) {
			String s = formulaList.get(i);

			if (s.equals("(")) {
				if (countLeftParentheses != 0) {
					function += s;
				}
				countLeftParentheses++;
				complex = true;

			} else if (s.equals(")")) {
				countRightParentheses++;
				if (countRightParentheses != countLeftParentheses) {
					function += s;
				}
				if (countLeftParentheses == countRightParentheses) {
					partFormula.add(function);
					complex = false;
					function = "";
					countLeftParentheses = 0;
					countRightParentheses = 0;
				}

			} else {
				if (complex) {
					function += s;
				} else {
					if (isOperation(s)) {
						partFormula.add(s);
					} else {
						int nextIndex = i + 1;
						if (nextIndex < formulaList.size() && !isOperation(formulaList.get(nextIndex)))
							function += s;
						else {
							function += s;
							partFormula.add(function);
							function = "";
						}
					}
				}
			}
		}
		return partFormula;
	}

	public void setFormulaAndVariable(String formula) throws Exception {
		
		String cleanFormula = formula.replaceAll(" ", "");
		int left = 0;
		int right = 0;
		
		for (int i = 0; i < cleanFormula.length(); i++) {
			char chr = cleanFormula.charAt(i);
			
			if (chr == '(') {
				left++;
			}
			if (chr == ')') {
				right++;
			}
		}
		if (left != right) {
			throw new Exception(left + " '(' and " + right + " ')' . Not balanced!");
		}

		List<String> formulaList = Arrays.asList(cleanFormula.split(""));

		CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
			setVariable(formulaList);
		});

		this.formula = new LinkedList<String>();

		StringBuilder function = new StringBuilder();

		boolean complex = false;

		int countLeftParentheses = 0;
		int countRightParentheses = 0;

		for (int i = 0; i < formulaList.size(); i++) {
			String s = formulaList.get(i);

			if (s.equals("(")) {
				if (countLeftParentheses != 0) {
					function.append(s);
				}
				countLeftParentheses++;
				complex = true;
			} else if (s.equals(")")) {
				countRightParentheses++;
				if (countRightParentheses != countLeftParentheses) {
					function.append(s);
				}
				if (countLeftParentheses == countRightParentheses) {
					this.formula.add(function.toString());
					complex = false;
					function = new StringBuilder();
					countLeftParentheses = 0;
					countRightParentheses = 0;
				}
			} else {

				if (complex) {
					function.append(s);
				} else {
					if (isOperation(s)) {
						this.formula.add(s);
					} else {
						int nextIndex = i + 1;
						if (nextIndex < formulaList.size() && !isOperation(formulaList.get(nextIndex)))
							function.append(s);
						else {
							function.append(s);
							this.formula.add(function.toString());
							function = new StringBuilder();
						}
					}
				}
			}
		}

		future.join();

	}

	public void setVariableVal(Map<String, String> vals) throws Exception {
		this.variableVal = new ArrayList<String>();
		Pattern pattern = Pattern.compile("^-?\\d+(\\.\\d+)?$");
		if (vals != null) {
			for (String variableName : this.variable) {
				String val = vals.get(variableName);
				if (val == null) {
					throw new Exception("In CalculatUtil, Your variable's value null! Your variableName: "+variableName+" Your value: "+val);
				} else if (val.isBlank() || val.isEmpty()) {
					throw new Exception("In CalculatUtil, Your variable's value Empty! Your variableName: "+variableName+" Your value: "+val);
				} else if ( ! pattern.matcher(val).matches()){
					throw new Exception("In CalculatUtil, Your variable's value is not Number! Your variableName: "+variableName+" Your value: "+val);
				}
				
				if (val != null && !val.isBlank() && !val.isEmpty()) {
					this.variableVal.add(val);
				}
			}
		}
	}

	public String runFormula() throws Exception {
		if (this.variable != null && this.variableVal != null && (this.variable.size() > this.variableVal.size())) {
			throw new Exception("There's variable isn't mapped");
		}

		for (int i = 0; i < this.formula.size(); i++) {
			String function = this.formula.get(i);
			try {
				if (!isOperation(function)) {
					new BigDecimal(function);
				}
			} catch (Exception e) {
				this.formula.remove(i);
				String regex = ".*[+\\-*/^].*";

				Pattern pattern = Pattern.compile(regex);
				if (pattern.matcher(function).matches()) {
					function = culculatePartExpression(function);
				} else {
					function = putVariable(function);
				}
				this.formula.add(i, function);

			}
		}
		BigDecimal filnalResult = BigDecimal.ZERO;

		filnalResult = simplifyFormula(this.formula, filnalResult);
		
		if (this.scale != null) {
			filnalResult = filnalResult.setScale(this.scale, RoundingMode.HALF_UP);
		}

		setAns(filnalResult);
		
		return filnalResult.toString();
	}

	public static boolean runComparativeExpression(String formulaLeft, Map<String, String> leftVariable,
			String comparativeOperation, String formulaRight, Map<String, String> rightVariable) throws Exception {
		CalculateUtil leftCalculateUtil = new CalculateUtil();
		CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
			try {
				leftCalculateUtil.setFormulaAndVariable(formulaLeft);
				if (leftVariable != null) {
					leftCalculateUtil.setVariableVal(leftVariable);
				}
				leftCalculateUtil.runFormula();
			} catch (Exception e) {
				throw new RuntimeException("Exception occurred during asynchronous task", e);
			}
		});
		CalculateUtil rightCalculateUtil = new CalculateUtil();
		rightCalculateUtil.setFormulaAndVariable(formulaRight);
		if (rightVariable != null) {
			rightCalculateUtil.setVariableVal(rightVariable);
		}
		rightCalculateUtil.runFormula();
		future.join();

		boolean fitOrNot = false;

		double leftAns = new BigDecimal(leftCalculateUtil.getAns()).doubleValue();
		
		double rightAns = new BigDecimal(rightCalculateUtil.getAns()).doubleValue();

		switch (comparativeOperation) {
		case ">": {
			fitOrNot = leftAns > rightAns;
			break;
		}
		case ">=", "≥", "≧": {
			fitOrNot = leftAns >= rightAns;
			break;
		}
        case "<": {
            fitOrNot = leftAns < rightAns;
            break;
		}
		case "<=", "≤", "≦": {
			fitOrNot = leftAns <= rightAns;
			break;
		}
        case "=": {
			fitOrNot = leftAns == rightAns;
			break;
		}
		case "!=", "≠": {
			fitOrNot = leftAns != rightAns;
			break;
		}

        }
		return fitOrNot;
	}

	private boolean isOperation(String s) {
		switch (s) {
		case "+", "-", "*", "/", "÷", "^":
			return true;
        default:
			return false;
		}
	}

	private BigDecimal simplifyFormula(LinkedList<String> targetFormula, BigDecimal result) throws Exception {

		calculatPow(targetFormula);

		calculatMultiply(targetFormula);

		calculatDivide(targetFormula);

		calculatAdd(targetFormula);

		calculatMinus(targetFormula);

		return new BigDecimal(targetFormula.get(0));
	}

	private String putVariable(String val) throws Exception {
		String num = val;
		if (this.variable.contains(val)) {

			int index = this.variable.indexOf(val);

			num = this.variableVal.get(index);
		} else if (val.equals("e") || val.equals("E")) {
			num =  String.valueOf(Math.E);
		}
		return num.toString();
	}

	private void expessionToNumber(String function, LinkedList<String> partFormula, int indexOfFormula)
			throws Exception {
		if (!isOperation(function)) {
			if (function.contains("+") && !function.startsWith("+")) {
				partFormula.remove(indexOfFormula);
				partFormula.add(indexOfFormula, calculatAdd(function));
			} else if (function.contains("-") && !function.startsWith("-")) {
				partFormula.remove(indexOfFormula);
				partFormula.add(indexOfFormula, calculatMinus(function));
			} else if (function.contains("*")) {
				partFormula.remove(indexOfFormula);
				partFormula.add(indexOfFormula, calculatMutiply(function));
			} else if (function.contains("/")) {
				partFormula.remove(indexOfFormula);
				partFormula.add(indexOfFormula, calculatDivide(function));
			} else if (function.contains("÷")) {
				partFormula.remove(indexOfFormula);
				partFormula.add(indexOfFormula, calculatDivide(function));
			} else if (function.contains("^")) {
				partFormula.remove(indexOfFormula);
				partFormula.add(indexOfFormula, calculatPow(function));
			}
		}
	}

	// 遞迴
	private String keepCalculate(String fraction) throws Exception {
		recursionCount++;
		BigDecimal filnalResult = BigDecimal.ZERO;

		LinkedList<String> partFormula = setPartFormula(fraction);
		for (int i = 0; i < partFormula.size(); i++) {
			String ele = partFormula.get(i);

			while (ele.contains("(") || ele.contains(")")) {

				partFormula.remove(i);

				ele = keepCalculate(ele);

				partFormula.add(i, calculatAdd(ele));

			}

			expessionToNumber(ele, partFormula, i);

		}

		filnalResult = simplifyFormula(partFormula, filnalResult);

		if (partFormula.size() == 1) {
			return partFormula.get(0);
		}
		return filnalResult.toString();
	}

	private String culculatePartExpression(String fraction) throws Exception {

		BigDecimal filnalResult = BigDecimal.ZERO;

		LinkedList<String> partFormula = setPartFormula(fraction);
		for (int i = 0; i < partFormula.size(); i++) {
			String ele = partFormula.get(i);

			if (ele.contains("(") || ele.contains(")")) {
				partFormula.remove(i);
				ele = keepCalculate(ele);
				partFormula.add(i, calculatAdd(ele));
			}

			expessionToNumber(ele, partFormula, i);

		}

		filnalResult = simplifyFormula(partFormula, filnalResult);

		if (partFormula.size() == 1) {
			return partFormula.get(0);
		}
		return filnalResult.toString();
	}

	private void calculatPow(LinkedList<String> partFormula) throws Exception {
		int startIndex = 0;
		int endIndex = 0;
		BigDecimal result = BigDecimal.ZERO;
		while (partFormula.contains("^")) {

			int operationIndex = partFormula.lastIndexOf("^");

			startIndex = operationIndex - 1;
			endIndex = operationIndex + 1;

			String baseString = partFormula.get(startIndex);
			String exponentString = partFormula.get(endIndex);

			BigDecimal base = new BigDecimal(putVariable(baseString));
			BigDecimal exponent = new BigDecimal(putVariable(exponentString));

			result = new BigDecimal(Math.pow(base.doubleValue(), exponent.doubleValue()), MathContext.DECIMAL128);

			partFormula.remove(startIndex);
			partFormula.remove(startIndex);
			partFormula.remove(startIndex);
			partFormula.add(startIndex, result.toString());
		}
	}

	private void calculatMultiply(LinkedList<String> partFormula) throws Exception {
		int startIndex = 0;
		int endIndex = 0;
		BigDecimal result = BigDecimal.ZERO;
		while (partFormula.contains("*")) {

			int operationIndex = partFormula.indexOf("*");

			startIndex = operationIndex - 1;
			endIndex = operationIndex + 1;

			String executorString = partFormula.get(startIndex);
			String targetString = partFormula.get(endIndex);

			BigDecimal executor = new BigDecimal(putVariable(executorString));
			BigDecimal target = new BigDecimal(putVariable(targetString));

			result = executor.multiply((target), MathContext.DECIMAL128);

			partFormula.remove(startIndex);
			partFormula.remove(startIndex);
			partFormula.remove(startIndex);
			partFormula.add(startIndex, result.toString());
		}
	}

	private void calculatDivide(LinkedList<String> partFormula) throws Exception {
		int startIndex = 0;
		int endIndex = 0;
		BigDecimal result = BigDecimal.ZERO;
		while (partFormula.contains("/") || partFormula.contains("÷")) {

			int operationIndex = partFormula.indexOf("/");
			if (operationIndex < 0) {
				operationIndex = partFormula.indexOf("÷");
			}

			startIndex = operationIndex - 1;
			endIndex = operationIndex + 1;

			String executorString = partFormula.get(startIndex);
			String targetString = partFormula.get(endIndex);

			BigDecimal executor = new BigDecimal(putVariable(executorString));
			BigDecimal target = new BigDecimal(putVariable(targetString));

			result = executor.divide((target), MathContext.DECIMAL128);

			partFormula.remove(startIndex);
			partFormula.remove(startIndex);
			partFormula.remove(startIndex);
			partFormula.add(startIndex, result.toString());
		}
	}

	private void calculatAdd(LinkedList<String> partFormula) throws Exception {
		int startIndex = 0;
		int endIndex = 0;
		BigDecimal result = BigDecimal.ZERO;
		while (partFormula.contains("+")) {

			int operationIndex = partFormula.indexOf("+");

			startIndex = operationIndex - 1;
			endIndex = operationIndex + 1;

			String executorString = partFormula.get(startIndex);
			String targetString = partFormula.get(endIndex);

			BigDecimal executor = new BigDecimal(putVariable(executorString));
			BigDecimal target = new BigDecimal(putVariable(targetString));

			result = executor.add((target), MathContext.DECIMAL128);

			partFormula.remove(startIndex);
			partFormula.remove(startIndex);
			partFormula.remove(startIndex);
			partFormula.add(startIndex, result.toString());
		}
	}

	private void calculatMinus(LinkedList<String> partFormula) throws Exception {
		int startIndex = 0;
		int endIndex = 0;
		BigDecimal result = BigDecimal.ZERO;
		while (partFormula.contains("-")) {

			int operationIndex = partFormula.indexOf("-");

			startIndex = operationIndex - 1;
			endIndex = operationIndex + 1;

			String executorString = partFormula.get(startIndex);
			String targetString = partFormula.get(endIndex);

			BigDecimal executor = new BigDecimal(putVariable(executorString));
			BigDecimal target = new BigDecimal(putVariable(targetString));

			result = executor.subtract((target), MathContext.DECIMAL128);

			partFormula.remove(startIndex);
			partFormula.remove(startIndex);
			partFormula.remove(startIndex);
			partFormula.add(startIndex, result.toString());
		}

	}

	private String calculatMinus(String partFormula) throws Exception {
		BigDecimal result = BigDecimal.ZERO;
		String[] numString = partFormula.split("-");
		BigDecimal executor = new BigDecimal(putVariable(numString[0]));
		BigDecimal target = new BigDecimal(putVariable(numString[1]));
		result = executor.subtract(target, MathContext.DECIMAL128);
		return result.toString();
	}

	private String calculatMutiply(String partFormula) throws Exception {
		BigDecimal result = BigDecimal.ONE;
		String[] numString = partFormula.split("\\*");
		for (String s : numString) {
			result = result.multiply(new BigDecimal(putVariable(s)), MathContext.DECIMAL128);
		}
		return result.toString();
	}

	private String calculatDivide(String partFormula) throws Exception {
		BigDecimal result = BigDecimal.ZERO;
		String[] numString = null;
		if (partFormula.contains("/")) {
			numString = partFormula.split("/");
		} else if (partFormula.contains("÷")) {
			numString = partFormula.split("÷");
		}
		BigDecimal divideChild = new BigDecimal(putVariable(numString[0]));
		BigDecimal divideParent = new BigDecimal(putVariable(numString[1]));
		result = divideChild.divide(divideParent, MathContext.DECIMAL128);
		return result.toString();
	}

	private String calculatPow(String partFormula) throws Exception {
		BigDecimal result = BigDecimal.ZERO;
		String[] numString = partFormula.split("^");
		BigDecimal base = new BigDecimal(putVariable(numString[0]));
		BigDecimal exponent = new BigDecimal(putVariable(numString[1]));
		result = new BigDecimal(Math.pow(base.doubleValue(), exponent.doubleValue()), MathContext.DECIMAL128);

		return result.toString();
	}

	private String calculatAdd(String partFormula) throws Exception {
		BigDecimal result = BigDecimal.ZERO;
		String[] numString = partFormula.split("\\+");
		for (String ele : numString) {

			result = result.add(new BigDecimal(putVariable(ele)));
		}

		return result.toString();
	}

	public static void main(String[] args) {
		CalculateUtil calculate = new CalculateUtil();
		try {
			String expression = "( ( (x + y)^z) + 9 ) * (x+z) + (5 * 9)^(7*2)+u+E^E";


			calculate.setFormulaAndVariable(expression);

			Map<String,String> map = new HashMap<String,String>();
			map.put("x","7");
			map.put("y","8");
			map.put("z","2");
			map.put("u","2");

			calculate.setVariableVal(map);

			calculate.runFormula();

//			Boolean ans = CalculateUtil.runComparativeExpression(formula, variableMap, "<", "2÷0.3", variableMap);

			System.out.println(calculate.getAns());
//			calculate.runFormula();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
