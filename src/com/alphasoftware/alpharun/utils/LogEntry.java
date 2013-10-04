package com.alphasoftware.alpharun.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogEntry {

	String logCommand;
	String logResult;
	long logDatetime;

	public LogEntry(){
		logCommand = "";
		logResult = "";
		logDatetime = -1;
	}

	public LogEntry(String command, String result, long datetime){
		this.logCommand = command;
		this.logResult = result;
		this.logDatetime = datetime;
	}

	private String convertTime(long time){
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		
		return df.format(new Date(time));

	}


	public String getLogCommand() {
		return logCommand;
	}

	public void setLogCommand(String logCommand) {
		this.logCommand = logCommand;
	}

	public String getLogResult() {
		return logResult;
	}

	public void setLogResult(String logResult) {
		this.logResult = logResult;
	}

	public String getLogDatetime() {
		return convertTime(logDatetime);
	}

	public void setLogDatetime(long logDatetime) {
		this.logDatetime = logDatetime;
	}

}
