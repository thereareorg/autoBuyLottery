import java.awt.*;
import java.awt.event.*;


public class BetXJSSCListener implements ActionListener
{
	private autoBet ab;
	private Client client;
	
	
	public BetXJSSCListener(autoBet ab, Client client) {
		this.ab = ab;
		this.client = client;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if(ab.loginToDSNMemberSuccess == false){			
			return;
		}
		
		//ab.grabThread.startGrabBJSC();
		
		String s = ab.textFieldXJSSCBetPercent.getText();
		
		if(Common.isNum(s)){
			double percent = Double.parseDouble(s);
			BetXJSSCThread.betXJSSCPercent = percent;
		}
		else{
			//TODO �����Ի�����ʾ�������
		}
		
		ab.setBetTime();
		
	
		BetXJSSCThread.betOppositeXJSSC = true;
		
		String outputStr = "��ʼ��Ͷ��������ʮ��,Ͷע������" + BetXJSSCThread.betXJSSCPercent + "\n";
		
		autoBet.outputGUIMessage(outputStr);
		
		BetXJSSCManager.showXJSSCDeatilsTable();
		
		if(ab.inBetXJSSC == true){					
			return;
		}
		

		
		BetXJSSCThread betThread = new BetXJSSCThread(client);
								
		betThread.start();
		
		ab.inBetXJSSC = true;
		
		
		
	}	

}


class BetAmountDetailXJSSCListener implements ActionListener
{
	private autoBet ab;
	
	
	
	public BetAmountDetailXJSSCListener(autoBet ab) {
		this.ab = ab;
	}
	
	public void actionPerformed(ActionEvent e)
	{

		BetXJSSCManager.showXJSSCBetAmountTable();
	}	

}
