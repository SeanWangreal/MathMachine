package com.mathMachine;

import java.util.HashMap;
import java.util.Map;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        try {
            String expression = "(((x + (y+(z+(x+(2+(x+7*(y+(z+(z+(x+1+(x+1+(x+1+(x+1+(x+1+(x+1+(x+1+(x+1+(x+1+(x+1+(x+1+(x+1+(x+1+(x+1+(x+1+(x+1+(x+1+(x+1+(x+1+(x+1+(x+1+(x+1+(x+1))+(x+1+(x+1+(x+1)))))))))))))))))))))))))))))))))^z) + 9) * (x + z) + (5 * 9)^2";

            CalculateUtil calculator = new CalculateUtil();

            calculator.setFormulaAndVariable(expression);

            Map<String,String> map = new HashMap<String,String>();
            map.put("x","7");
            map.put("y","8");
            map.put("z","2");

            calculator.setVariableVal(map);

            calculator.runFormula();

            String ans = calculator.getAns();
            int recursionCount = calculator.getRecursionCount();
            System.out.println(ans);
            System.out.println(recursionCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}