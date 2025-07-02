"# MathExpressionCalculate"   
  Ex: Put String expression in CalculateUtil and get Answer

  Expression = (((x + y)^z) + 9) * (x + z) + (5 * 9)^2  
    x = 7  
  y = 8  
  z = 2  
Like 

	try {
 		String expression = "(((x + y)^z) + 9) * (x + z) + (5 * 9)^2";
   
		CalculateUtil calculator = new CalculateUtil();
	
		calculator.setFormulaAndVariable(expression);
	
		Map<String,String> map = new HashMap<String,String>();
		map.put("x","7");
		map.put("y","8");
		map.put("z","2");
	
		calculator.setVariableVal(map);
	
		calculator.runFormula();
	        System.out.println(calculator.getAns()) // 4131
	} catch (Exception e) {
		e.printStackTrace();
	}


