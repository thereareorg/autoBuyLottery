import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;


public class BetXJSSCManager {
	
	static long XJSSCloseTime = 0;    //封盘时间
	
	static String XJSSCoddsData = null;
	
	
	static String previousXJSSCdrawNumber = "";
	
    static String XJSSCdrawNumber = "";
    static String previousXJSSCBetNumber = "";
    
    static boolean previousXJSSCBetResult = false;
    
    static Vector<String> unCalcProfitXJSSCDraw = new Vector<String>();    
    
    
    static int XJSSCbetTotalAmount = 0;
    
    static DSNDataDetailsWindow XJSSCdetalsDataWindow = new DSNDataDetailsWindow();   
    
    static Vector<String> unknowStatXJSSCDraw = new Vector<String>();    
    
    static DSNBetAmountWindow XJSSCBetAmountWindow = new DSNBetAmountWindow();    
    
    static int XJSSCbishu = 0;    
    static int XJSSConeBetAmount = 0;    
    
    static int XJSSCzongqishu = 0;
    static int XJSSCzongshibai = 0;
    static int XJSSCzongyichang = 0;
    
    static int XJSSCjinriqishu = 0;
    static int XJSSCjinrishibai = 0;
    static int XJSSCjinriyichang = 0;    
    
    
    
    static long XJSSCremainTime = -1;

	
	public static long getXJSSCremainTime(){
        //get period
    	String response = "";
    	String host = dsnHttp.ADDRESS;
    		
        String getTimeUrl = host + "/time?&_=";
        getTimeUrl += Long.toString(System.currentTimeMillis());
        
        long startTime = System.currentTimeMillis();
        
        response = dsnHttp.doGet(getTimeUrl, "", dsnHttp.ADDRESS + "/member/load?lottery=XJSSC&page=lm");
        
        if(response == null){//再拿一次
        	startTime = System.currentTimeMillis();
        	response = dsnHttp.doGet(getTimeUrl, "", dsnHttp.ADDRESS + "/member/load?lottery=XJSSC&page=lm");
        }
        
        long endTime = System.currentTimeMillis();
        
        long requestTime = endTime - startTime;
        
        requestTime = requestTime /2;
        
        
        if(response != null && Common.isNum(response))
        {
        	dsnHttp.time = Long.parseLong(response);
        	dsnHttp.timeDValue = dsnHttp.time + requestTime - System.currentTimeMillis();
        }
        else{
        	dsnHttp.time = System.currentTimeMillis();
        }
    	
        if(!isInXJSSCBetTime(dsnHttp.time)){
        	return -1;
        }
        
        
        String getPeriodUrl = host + "/member/period?lottery=XJSSC&_=";
        getPeriodUrl += Long.toString(System.currentTimeMillis());

        //System.out.println(cookieb18 + "defaultLT=XJSSC;" + cookieCfduid);
        response = dsnHttp.doGet(getPeriodUrl, "", dsnHttp.ADDRESS + "/member/load?lottery=XJSSC&page=lm");
        
        if(response == null){
        	dsnHttp.addFailsTimes();
        	response = dsnHttp.doGet(getPeriodUrl, "", dsnHttp.ADDRESS + "/member/load?lottery=XJSSC&page=lm");
        }
        
        if(response == null)
        {
        	dsnHttp.addFailsTimes();
        	System.out.println("get period failed");
        	return System.currentTimeMillis();
        }
        
        
        
        System.out.println("preiod:");
        System.out.println(response);
        
        
        try{
            JSONObject periodJson = new JSONObject(response);
            XJSSCloseTime = periodJson.getLong("closeTime");
            if(!XJSSCdrawNumber.equals(periodJson.getString("drawNumber"))) {//新的一期
            	previousXJSSCdrawNumber = XJSSCdrawNumber;
            	XJSSCdrawNumber = periodJson.getString("drawNumber");
            	if(previousXJSSCBetNumber != previousXJSSCdrawNumber && previousXJSSCBetNumber != XJSSCdrawNumber && previousXJSSCBetNumber != "") {//判断上一期有没有漏投
					long dNum = 0;
					try {
					    long drawNum = Long.parseLong(XJSSCdrawNumber)%1000;
					    long preBetNum =  Long.parseLong(previousXJSSCBetNumber)%1000;
					    if(drawNum - preBetNum > 0) {
					    	dNum = drawNum - preBetNum - 1;
					    }else if(drawNum - preBetNum  < 0){
					    	dNum = drawNum + 120 - preBetNum - 1;
					    }
					} catch (NumberFormatException e) {
					    e.printStackTrace();
					}
					
					
					//failTimes += dNum;
					
					XJSSCjinrishibai += dNum;					
			        XJSSCjinriqishu += dNum;
			        
			        XJSSCdetalsDataWindow.updateTextFieldjinriqishu(Integer.toString(XJSSCjinriqishu));
			        XJSSCdetalsDataWindow.updateTextFieldjinrishibai(Integer.toString(XJSSCjinrishibai));
			        
			        XJSSCdetalsDataWindow.updateTextFieldzongqishu(Integer.toString(XJSSCzongqishu + XJSSCjinriqishu));
			        XJSSCdetalsDataWindow.updateTextFieldzongshibai(Integer.toString(XJSSCzongshibai + XJSSCjinrishibai));
					
					
					
/*					autoBet.labelFailBets.setText("失败次数:" + failTimes);
					autoBet.labelTotalBets.setText("下单次数:" + (successTimes + failTimes));*/
					System.out.println("漏投" + dNum + "次, 期数：" + XJSSCdrawNumber + "上次下单期数：" + previousXJSSCBetNumber);
					
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm");//设置日期格式
					long missDrawNumber = Long.parseLong(previousXJSSCBetNumber) + 1;
					for(int i = 0; i < dNum; i++){
						String missDrawNmberstr = Long.toString(missDrawNumber + i);						
		    			XJSSCdetalsDataWindow.addData(df.format(new Date()), missDrawNmberstr, 3, "---", "---"); 
					}
					
					
					previousXJSSCBetNumber = previousXJSSCdrawNumber;
					
					
			    }
            }
        }
        catch(Exception e){
        	autoBet.outputGUIMessage("获取迪斯尼时间错误！");
        	System.out.println("getXJSSCRemainTime()获取时间异常" + response);
        	return System.currentTimeMillis();
        }
        
        

       
        
        long remainTime = XJSSCloseTime - (System.currentTimeMillis() + dsnHttp.timeDValue); //用差值计算  防止两次请求期间间隔过长
        
        XJSSCremainTime = remainTime;
        
    	return remainTime;
	}
	
	
	
	
    public static boolean  isInXJSSCBetTime(long time){
        Date date = new Date(time);
        int currentHour = date.getHours();
        int currentMinutes = date.getMinutes();
        int currentSeconds = date.getSeconds();
        
        /*if(currentHour >=9 && (currentHour * 60 + currentMinutes <= 23 * 60 + 57)){
        	return true;
        }*/
        
        //两分钟分钟的缓冲
        if((currentHour*60 + currentMinutes < 10*60 + 1) && (currentHour * 60 + currentMinutes >= 2 * 60))
            return false;
         
         return true;
                        
    }
    
    
    public static long getXJSSClocalRemainTime() {
    	XJSSCremainTime = XJSSCloseTime - (System.currentTimeMillis() + dsnHttp.timeDValue);
    	return XJSSCremainTime;
    }
    
    
    public static String getXJSSCoddsData(){
    	String url = dsnHttp.ADDRESS + "/member/odds?lottery=XJSSC&games=ZDS%2CZDX%2CZWDX%2CYDX1%2CYDX2%2CYDX3%2CYDX4%2CYDX5%2CYDS1%2CYDS2%2CYDS3%2CYDS4%2CYDS5%2CYWDX1%2CYWDX2%2CYWDX3%2CYWDX4%2CYWDX5%2CYHDS1%2CYHDS2%2CYHDS3%2CYHDS4%2CYHDS5%2CLH15&_=";
    	url += Long.toString(System.currentTimeMillis());
    	
    	XJSSCoddsData = dsnHttp.doGet(url, "", "");
    	
    	if(XJSSCoddsData == null){
    		XJSSCoddsData = dsnHttp.doGet(url, "", "");
    	}
    	
    	return  XJSSCoddsData;
   	
    }
    
    
    public static boolean doBetXJSSC(String[] betData, double percent, boolean opposite, String remainTime)
    {

    	String host = dsnHttp.ADDRESS;
       	
        String jsonParam = "";
        
        if(previousXJSSCBetNumber.equals(XJSSCdrawNumber)) 
        	return false;
        
        
        XJSSCjinriqishu++;
        
        XJSSCdetalsDataWindow.updateTextFieldjinriqishu(Integer.toString(XJSSCjinriqishu));
        
        XJSSCdetalsDataWindow.updateTextFieldzongqishu(Integer.toString(XJSSCzongqishu + XJSSCjinriqishu));
        
        //如果未到封盘时间
        if( XJSSCdrawNumber != null){
        	
        	//System.out.printf("下注重庆时时彩第%s期\n",XJSSCdrawNumber);
        	String outputStr = "下注新疆时时彩第" + XJSSCdrawNumber + "期\n" + "最新数据时间距收盘" + remainTime + "秒\n";
        	autoBet.outputGUIMessage(outputStr);
        	
/*        	if(isEmptyData(betData, BetType.XJSSC)) {
        		outputStr = "代理无人投注\n\n";
        		autoBet.outputGUIMessage(outputStr);
        		return false;
        	}*/
        	
        	jsonParam = constructBetsData(betData, percent, BetType.XJSSC, opposite);
        	
        	//在投注金额窗口显示,只显示百分之一
        	String jsonStr = constructBetsData(betData, 0.01, BetType.XJSSC, opposite);
        	
        	
        	
        	
        	if(jsonParam == "") {
        		
        		previousXJSSCBetNumber = XJSSCdrawNumber;
        		
        		outputStr = "代理无人投注\n\n";
        		autoBet.outputGUIMessage(outputStr);

        		return false;
        	}
        
        	addToBetAmountWindow(jsonStr, BetType.XJSSC);
        	
        	outputBetsDetails(jsonParam);	
        	
        	System.out.println(jsonParam);
        	
        	String response = "";
        	
        	

        	

        	long time1 = System.currentTimeMillis();
        	
        	response = dsnHttp.bet(host + "/member/bet", jsonParam, "UTF-8", "");
        	
        	
        	boolean betRes = isBetSuccess(XJSSCdrawNumber);
        	
        	boolean result = dsnHttp.pureParseBetResult(response);
        	
        	if((betRes == false) && (result == false)){
        		response = dsnHttp.bet(host + "/member/bet", jsonParam, "UTF-8", "");
        	}
        	

        	long time2 = System.currentTimeMillis();
        	
        	long timeD = time2 - time1;
        	
        	double usingTime = timeD;
        	
        	usingTime = usingTime/1000;
        	
        	String strUsingTime  = String.format("下单用时！ :%f 秒\n", usingTime);
        	
        	autoBet.outputGUIMessage(strUsingTime);

        	
        	result = dsnHttp.parseBetResult(response);
        	
        	
        	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm");//设置日期格式
        	
        	if(result == false){
    			XJSSCdetalsDataWindow.addData(df.format(new Date()), XJSSCdrawNumber, 2, Integer.toString(XJSSCbetTotalAmount), Integer.toString(XJSSCbishu));

    			unknowStatXJSSCDraw.add(XJSSCdrawNumber);
    			
    			XJSSCdetalsDataWindow.updateTextFieldjinriyichang(Integer.toString(unknowStatXJSSCDraw.size()));
    			
        	}
        	else{
    			XJSSCdetalsDataWindow.addData(df.format(new Date()), XJSSCdrawNumber, 0, Integer.toString(XJSSCbetTotalAmount), Integer.toString(XJSSCbishu));        		
        	}
        	
        	unCalcProfitXJSSCDraw.add(XJSSCdrawNumber);
        	

        	    
        	if(!previousXJSSCBetNumber.equals(XJSSCdrawNumber)) { //避免重复计数
/*        		if(result == true) {
					successTimes++;
					autoBet.labelSuccessBets.setText("成功次数:" + successTimes);
				} else {
					failTimes++;
					autoBet.labelFailBets.setText("失败次数:" + failTimes);
				}
				
				autoBet.labelTotalBets.setText("下单次数:" + (successTimes + failTimes));*/
        	} else if(result) {
/*        		successTimes++;
    			autoBet.labelSuccessBets.setText("成功次数:" + successTimes);
    			failTimes--;
				autoBet.labelFailBets.setText("失败次数:" + failTimes);*/
        	}
			
			previousXJSSCBetNumber = XJSSCdrawNumber;
        	previousXJSSCBetResult = result;
        	
        	return result;
        
        }
        
        return false;
    }
    
    
    
    public static String constructBetsData(String[] data, double percent, BetType betType, boolean opposite)
    {
    	
    	//data = "[[{\"k\":\"DX2\",\"i\":\"X\",\"c\":2,\"a\":55,\"r\":109.989,\"cm\":0},{\"k\":\"DX3\",\"i\":\"D\",\"c\":3,\"a\":130,\"r\":258.294,\"cm\":0},{\"k\":\"DX4\",\"i\":\"D\",\"c\":4,\"a\":660,\"r\":1319.868,\"cm\":0},{\"k\":\"DS1\",\"i\":\"D\",\"c\":1,\"a\":10,\"r\":19.998,\"cm\":0},{\"k\":\"DX2\",\"i\":\"D\",\"c\":3,\"a\":20,\"r\":39.996,\"cm\":0},{\"k\":\"DS4\",\"i\":\"D\",\"c\":3,\"a\":20,\"r\":39.996,\"cm\":0},{\"k\":\"DX3\",\"i\":\"X\",\"c\":1,\"a\":5,\"r\":9.999,\"cm\":0},{\"k\":\"DX5\",\"i\":\"D\",\"c\":2,\"a\":40,\"r\":79.992,\"cm\":0},{\"k\":\"DX1\",\"i\":\"X\",\"c\":2,\"a\":55,\"r\":109.989,\"cm\":0},{\"k\":\"DX4\",\"i\":\"X\",\"c\":1,\"a\":40,\"r\":79.992,\"cm\":0},{\"k\":\"ZDX\",\"i\":\"D\",\"c\":2,\"a\":15,\"r\":29.997,\"cm\":0},{\"k\":\"DS1\",\"i\":\"S\",\"c\":2,\"a\":15,\"r\":29.997,\"cm\":0},{\"k\":\"DS2\",\"i\":\"D\",\"c\":3,\"a\":100,\"r\":199.98,\"cm\":0},{\"k\":\"DS3\",\"i\":\"D\",\"c\":2,\"a\":55,\"r\":109.989,\"cm\":0},{\"k\":\"DS3\",\"i\":\"S\",\"c\":3,\"a\":20,\"r\":39.996,\"cm\":0},{\"k\":\"DS4\",\"i\":\"S\",\"c\":2,\"a\":45,\"r\":89.991,\"cm\":0},{\"k\":\"DS5\",\"i\":\"D\",\"c\":3,\"a\":50,\"r\":99.99,\"cm\":0},{\"k\":\"DX1\",\"i\":\"D\",\"c\":3,\"a\":50,\"r\":99.99,\"cm\":0},{\"k\":\"DX5\",\"i\":\"X\",\"c\":3,\"a\":40,\"r\":79.992,\"cm\":0},{\"k\":\"ZDS\",\"i\":\"S\",\"c\":2,\"a\":15,\"r\":29.997,\"cm\":0},{\"k\":\"DS2\",\"i\":\"S\",\"c\":2,\"a\":40,\"r\":79.992,\"cm\":0},{\"k\":\"DS5\",\"i\":\"S\",\"c\":2,\"a\":55,\"r\":109.989,\"cm\":0}],{\"DS1_S\":1.983,\"DS1_D\":1.983,\"DS2_S\":1.983,\"DS2_D\":1.983,\"DS3_S\":1.983,\"DS3_D\":1.983,\"DS4_S\":1.983,\"DS4_D\":1.983,\"DS5_S\":1.983,\"DS5_D\":1.983,\"DX1_X\":1.983,\"DX1_D\":1.983,\"DX2_X\":1.983,\"DX2_D\":1.983,\"DX3_X\":1.983,\"DX3_D\":1.983,\"DX4_X\":1.983,\"DX4_D\":1.983,\"DX5_X\":1.983,\"DX5_D\":1.983,\"LH_T\":9.28,\"LH_H\":1.983,\"LH_L\":1.983,\"ZDS_S\":1.983,\"ZDS_D\":1.983,\"ZDX_X\":1.983,\"ZDX_D\":1.983},{\"B1\":64,\"B4\":142,\"LM\":1535,\"B3\":64,\"B5\":334,\"B2\":64}]";
    	int totalAmount = 0;
    	
    	String res = "";
    	
    	String oddsData = "";
    	
    	
    	try{
    		
    		List<String> parsedGames = new ArrayList<String>();;
    		
	    	JSONArray gamesArray = new JSONArray();
	    	JSONObject oddsGrabData = null;


	    		
    		if(XJSSCoddsData == null){
    			getXJSSCoddsData();
    		}
	    		
	    	oddsData = XJSSCoddsData;
	    	
	    	
	    	oddsGrabData = new JSONObject(oddsData);
	    	
	    	for(int i = 0; i < data.length; i++){
    		
    		
            	JSONArray XJSSCLMGrabData = new JSONArray(data[i]);        	
            	JSONArray gamesGrabData = XJSSCLMGrabData.getJSONArray(0);

        	
	        	for(int j = 0; j < gamesGrabData.length(); j++){
	        		JSONObject gameGrabData = gamesGrabData.getJSONObject(j);
	        		
	    			String game = gameGrabData.getString("k");
	    			String contents = gameGrabData.getString("i");
	    			int amount = gameGrabData.getInt("a");
	    			String oddsKey = game + "_" + contents;
	    			
	    			if(oddsData.contains(oddsKey) == false)
	    				continue;
	    			
	    			double odds = oddsGrabData.getDouble(oddsKey);
	    			
	    			//剔除北京赛车冠亚军 和 两面
	    			/*if(game.indexOf("GDX") != -1 || game.indexOf("GDS") != -1)
	    				continue;*/
	    			
	    			if(parsedGames.contains(game) == true)
	    				continue;
	    			
	    			//计算差值
	        		for(int k = j +1 ; k < gamesGrabData.length(); k++){
	        			JSONObject oppositeGameGrabData = gamesGrabData.getJSONObject(k);
	        			String oppositeGame = oppositeGameGrabData.getString("k");
	        			if(oppositeGame.equals(game)){
	        				int oppositeAmount = oppositeGameGrabData.getInt("a");
	        				if(oppositeAmount > amount){
	        					amount = oppositeAmount - amount;
	        					contents = oppositeGameGrabData.getString("i");
	        					oddsKey = oppositeGame + "_" + contents;
	        					odds = oddsGrabData.getDouble(oddsKey);
	        				}
	        				else{
	        					amount = amount - oppositeAmount;
	        				}
	        				break;
	        			}
	        		}
	        		
	        		parsedGames.add(game);    			
	    			
	    			//只下赔率二以下的
	        		if(odds < 2.5 && amount >0){
	        			amount = (int)(amount*percent);  
	        			if(amount == 0)
	        				amount = 1;
	        			totalAmount += amount;
	        			
	        			JSONObject gameObj = new JSONObject();
	        			gameObj.put("game", game);
	        			
	        			//处理反投: 大变小，小变大，单变双，双变大，龙变虎，虎变隆
	        			if(opposite){
	        				if(game.indexOf("DX") != -1){//反大小
	        					if(contents.indexOf("D") != -1){
	        						contents = "X";        						
	        					}
	        					else{
	        						contents = "D";
	        					}
	        					oddsKey = game + "_" + contents;
	        					odds = oddsGrabData.getDouble(oddsKey);
	        				}
	        				
	        				
	        				if(game.indexOf("DS") != -1){//反单双
	        					if(contents.indexOf("D") != -1){
	        						contents = "S";        						
	        					}
	        					else{
	        						contents = "D";
	        					}
	        					oddsKey = game + "_" + contents;
	        					odds = oddsGrabData.getDouble(oddsKey);
	        				}
	        				
	        				if(game.indexOf("LH") != -1){//反龙虎
	        					if(contents.indexOf("L") != -1){
	        						contents = "H";        						
	        					}
	        					else{
	        						contents = "L";
	        					}
	        					oddsKey = game + "_" + contents;
	        					odds = oddsGrabData.getDouble(oddsKey);
	
	        				}
	
	        				
	        			}
	        			//反投处理结束
	        				        			
	        				        			
	        			gameObj.put("contents", contents);
	        			gameObj.put("amount", amount);
	        			gameObj.put("odds", odds);
	        			
	        			

	        			
	        			gamesArray.put(gameObj);
	        		}
	        		
	        	}
	    	}
	    	
	
	    	
	    	if(gamesArray.length() == 0) {
	    		return "";
	    	}
	    	
	    	JSONObject betsObj = new JSONObject();
	    	
	    	boolean ignore = false;
	    	betsObj.put("ignore", ignore);
	    	betsObj.put("bets", gamesArray);
	    	

        	betsObj.put("drawNumber",XJSSCdrawNumber);
        	
        	betsObj.put("lottery", "XJSSC");

	    	
	    	res = betsObj.toString();
    	
    	}catch(Exception e){
    		autoBet.outputGUIMessage("构造下单数据错误！\n");
    		return "";
    	}
   	
    	return res;	
    }
    
    
    
    public static boolean isBetSuccess(String drawNumber){
    	return true;
    }
    
    public static void showXJSSCDeatilsTable(){
    	XJSSCdetalsDataWindow.setVisible(true);
    }
    
    public static String getXJSSCdrawNumber(){
    	return XJSSCdrawNumber;
    }
    
    
    public static void outputBetsDetails(String jsonData){
    	
    	autoBet.outputGUIMessage("下注详情：\n");
    	try{

            	
        	JSONObject betsData = new JSONObject(jsonData);
        	JSONArray gamesData = betsData.getJSONArray("bets");
        	int totalAmount = 0;
        	
        	for(int i = 1; i <= 5 ; i++){
        		String gameDX = "DX" + Integer.toString(i);
        		String gameDS = "DS" + Integer.toString(i);        		
        		JSONObject gameData;
        		int amountDX = 0;
        		String contentsDX = "";
        		int amountDS = 0;
        		String contentsDS = "";
        		
        		XJSSCbishu = gamesData.length();
        		
        		for(int j = 0; j < gamesData.length(); j++){
        			gameData = gamesData.getJSONObject(j);
        			String game = gameData.getString("game");
        			if(game.equals(gameDX)){
        				amountDX = gameData.getInt("amount");
        				contentsDX = gameData.getString("contents");
        				contentsDX = contentsDX.equals("D")?"大":"小";
        			}
        			if(game.equals(gameDS)){
        				amountDS = gameData.getInt("amount");  
        				contentsDS = gameData.getString("contents");
        				contentsDS = contentsDS.equals("D")?"单":"双";
        			}
        			

        		}
        		
        		
    			String outputStr = "";
    			
				if(amountDX != 0 ){
						outputStr  = String.format("第%s球%s: %d,", Integer.toString(i), contentsDX, amountDX);
						autoBet.outputGUIMessage(outputStr);
						totalAmount += amountDX;
				}
				
				if(amountDS != 0 ){
					outputStr  = String.format("第%s球%s: %d,", Integer.toString(i), contentsDS, amountDS);
					autoBet.outputGUIMessage(outputStr);
					totalAmount += amountDS;
				}
				
				autoBet.outputGUIMessage("\n");
        		
        	}
        	
        	
        	String gameZDX = "ZDX";
        	String gameZDS = "ZDS";
        	String gameLH = "LH";
        	
        	int amountZDX = 0;
        	int amountZDS = 0;
        	int amountLH = 0;
        	
        	String contentsZDX = "";
        	String contentsZDS = "";
        	String contentsLH = "";
        	
    		for(int j = 0; j < gamesData.length(); j++){
    			JSONObject gameData;
    			gameData = gamesData.getJSONObject(j);
    			String game = gameData.getString("game");
    			
    			if(game.equals(gameZDX)){
    				amountZDX = gameData.getInt("amount");
    				contentsZDX = gameData.getString("contents");
    				contentsZDX = contentsZDX.equals("D")?"大":"小";
    			}
    			if(game.equals(gameZDS)){
    				amountZDS = gameData.getInt("amount");  
    				contentsZDS = gameData.getString("contents");
    				contentsZDS = contentsZDS.equals("D")?"单":"双";
    			}
				if(game.equals(gameLH)){
					amountLH = gameData.getInt("amount");
					contentsLH = gameData.getString("contents");
					contentsLH = contentsLH.equals("L")?"龙":"虎";
				}
	
    		}
    		
			String outputStr = "";
			if(amountZDX != 0){
				outputStr  = String.format("总%s: %d,",  contentsZDX, amountZDX);
				autoBet.outputGUIMessage(outputStr);
				totalAmount += amountZDX;
			}
			
			if(amountZDS != 0){
				outputStr  = String.format("总%s: %d,",  contentsZDS, amountZDS);
				autoBet.outputGUIMessage(outputStr);
				totalAmount += amountZDS;
			}
			
			if(amountLH != 0){
				outputStr  = String.format("%s: %d,",  contentsLH, amountLH);
				autoBet.outputGUIMessage(outputStr);
				totalAmount += amountLH;
			}
			
			autoBet.outputGUIMessage("\n");
			autoBet.outputGUIMessage("下单总金额:" + totalAmount +"\n");
			
			XJSSCbetTotalAmount = totalAmount;
    			
        	
    	}catch(Exception e){
    		e.printStackTrace();
    	}

    	
    }
    
    
    
    public static void addToBetAmountWindow(String jsonData, BetType betType){
    	
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm");//设置日期格式
    	
    	
    	try{


        	JSONObject betsData = new JSONObject(jsonData);
        	JSONArray gamesData = betsData.getJSONArray("bets");
        	int totalAmount = 0;
        	
        	for(int i = 1; i <= 5 ; i++){
        		String gameDX = "DX" + Integer.toString(i);
        		String gameDS = "DS" + Integer.toString(i);        		
        		JSONObject gameData;
        		int amountDX = 0;
        		String contentsDX = "";
        		int amountDS = 0;
        		String contentsDS = "";
        		
        		XJSSCbishu = gamesData.length();
        		
        		for(int j = 0; j < gamesData.length(); j++){
        			gameData = gamesData.getJSONObject(j);
        			String game = gameData.getString("game");
        			if(game.equals(gameDX)){
        				amountDX = gameData.getInt("amount");
        				contentsDX = gameData.getString("contents");
        				contentsDX = contentsDX.equals("D")?"大":"小";
        			}
        			if(game.equals(gameDS)){
        				amountDS = gameData.getInt("amount");  
        				contentsDS = gameData.getString("contents");
        				contentsDS = contentsDS.equals("D")?"单":"双";
        			}
        			

        		}
        		
        		
    			String outputStr = "";
    			
				if(amountDX != 0 ){
						outputStr  = String.format("第%s球  %s", Integer.toString(i), contentsDX);
						
						XJSSCBetAmountWindow.addData(df.format(new Date()), XJSSCdrawNumber, outputStr, Integer.toString(amountDX));

				}
				
				if(amountDS != 0 ){
					outputStr  = String.format("第%s球  %s", Integer.toString(i), contentsDS);
					
					XJSSCBetAmountWindow.addData(df.format(new Date()), XJSSCdrawNumber, outputStr, Integer.toString(amountDS));
					
				}
				
				
        		
        	}
        	
        	
        	String gameZDX = "ZDX";
        	String gameZDS = "ZDS";
        	String gameLH = "LH";
        	
        	int amountZDX = 0;
        	int amountZDS = 0;
        	int amountLH = 0;
        	
        	String contentsZDX = "";
        	String contentsZDS = "";
        	String contentsLH = "";
        	
    		for(int j = 0; j < gamesData.length(); j++){
    			JSONObject gameData;
    			gameData = gamesData.getJSONObject(j);
    			String game = gameData.getString("game");
    			
    			if(game.equals(gameZDX)){
    				amountZDX = gameData.getInt("amount");
    				contentsZDX = gameData.getString("contents");
    				contentsZDX = contentsZDX.equals("D")?"大":"小";
    			}
    			if(game.equals(gameZDS)){
    				amountZDS = gameData.getInt("amount");  
    				contentsZDS = gameData.getString("contents");
    				contentsZDS = contentsZDS.equals("D")?"单":"双";
    			}
				if(game.equals(gameLH)){
					amountLH = gameData.getInt("amount");
					contentsLH = gameData.getString("contents");
					contentsLH = contentsLH.equals("L")?"龙":"虎";
				}
	
    		}
    		
			String outputStr = "";
			if(amountZDX != 0){
				outputStr  = String.format("总%s",  contentsZDX);
				
				
				XJSSCBetAmountWindow.addData(df.format(new Date()), XJSSCdrawNumber, outputStr, Integer.toString(amountZDX));
				
				
			}
			
			if(amountZDS != 0){
				outputStr  = String.format("总%s",  contentsZDS);
				XJSSCBetAmountWindow.addData(df.format(new Date()), XJSSCdrawNumber, outputStr, Integer.toString(amountZDS));
				
			}
			
			if(amountLH != 0){
				outputStr  = String.format("%s",  contentsLH);
				XJSSCBetAmountWindow.addData(df.format(new Date()), XJSSCdrawNumber, outputStr, Integer.toString(amountLH));
				
			}


    			
        	
    	}catch(Exception e){
    		e.printStackTrace();
    	}

    	
    }
    
    
    public static Vector<String> getUnCalcProfitBJSCDraw(){
    	return unCalcProfitXJSSCDraw;
    }
    
    public static void updateUnCalcXJSSCDraw(Vector<String> calcedDraw){
    	for(int i =0; i < calcedDraw.size(); i++){
    		unCalcProfitXJSSCDraw.removeElement(calcedDraw.elementAt(i));
    		unknowStatXJSSCDraw.removeElement(calcedDraw.elementAt(i));
    	}  
    	
    	
    	long currentDraw = Long.parseLong(XJSSCdrawNumber);
    	
    	for(int j =0; j < unCalcProfitXJSSCDraw.size(); j++){
    		long idrawNumber = Long.parseLong(unCalcProfitXJSSCDraw.elementAt(j));
    		if((currentDraw - idrawNumber) >= 4){
    			unCalcProfitXJSSCDraw.removeElement(unCalcProfitXJSSCDraw.elementAt(j));
    		}
    	}
    	
    	
    	int yichangshu1 = unknowStatXJSSCDraw.size();
    	
    	for(int j =0; j < unknowStatXJSSCDraw.size(); j++){
    		long idrawNumber = Long.parseLong(unknowStatXJSSCDraw.elementAt(j));
    		if((currentDraw - idrawNumber) >= 4){
    			updateXJSSCWindowdetailsData(unknowStatXJSSCDraw.elementAt(j), TYPEINDEX.STATC.ordinal(), "1");
    			unknowStatXJSSCDraw.removeElement(unknowStatXJSSCDraw.elementAt(j));

    		}
    	}
    	
    	int yichangshu2 = unknowStatXJSSCDraw.size();
    	
    	XJSSCjinriyichang = unknowStatXJSSCDraw.size();
    	
    	XJSSCjinrishibai += yichangshu1 - yichangshu2;
    	
    	
    	
    	
    	
    	XJSSCdetalsDataWindow.updateTextFieldjinrishibai(Integer.toString(XJSSCjinrishibai));
    	XJSSCdetalsDataWindow.updateTextFieldjinriyichang(Integer.toString(XJSSCjinriyichang));
    	
    	
    	
    	XJSSCdetalsDataWindow.updateTextFieldzongqishu(Integer.toString(XJSSCzongqishu + XJSSCjinriqishu));
    	
    	XJSSCdetalsDataWindow.updateTextFieldzongshibai(Integer.toString(XJSSCzongshibai + XJSSCjinrishibai));
    	
    	
    }
    
    public static void updateXJSSCWindowdetailsData(String drawNumber, int index, String value){
    	XJSSCdetalsDataWindow.updateRowItem(drawNumber, index, value);
    }
    
    public static void showXJSSCBetAmountTable(){
    	XJSSCBetAmountWindow.setVisible(true);
    }
    
    public static void updateXJSSCBalance(String str){
    	XJSSCdetalsDataWindow.updateTextFieldyue(str);
    }
    
    
    public static boolean isXJSSCidle(){
    	boolean isIdle = false;
    	if(XJSSCremainTime < 0 || XJSSCremainTime > 25*1000 ){
    		isIdle = true;
    	}
    	
    	return isIdle;
    }
    
    
    public static void clearXJSSCdetalsData(){
    	if(unCalcProfitXJSSCDraw.size() != 0){
    		unCalcProfitXJSSCDraw.clear();
    	}
    	
    	if(unknowStatXJSSCDraw.size() != 0){
    		XJSSCjinrishibai += unknowStatXJSSCDraw.size();
    		unknowStatXJSSCDraw.clear();
    	}

    	XJSSCzongqishu += XJSSCjinriqishu;
    	XJSSCzongshibai += XJSSCjinrishibai;
    	

    	
    	XJSSCjinriqishu = 0;
    	XJSSCjinrishibai = 0;
    	XJSSCjinriyichang = 0;
    	
    	XJSSCremainTime = -1;

    }
    
    
}
