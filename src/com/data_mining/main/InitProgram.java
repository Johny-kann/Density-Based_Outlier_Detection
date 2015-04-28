package com.data_mining.main;

import com.data_mining.constants.FilesList;
import com.data_mining.controller.MainController;
import com.data_mining.file_readers.TextFileWriter;
import com.data_mining.logic.CommonLogics;
import com.data_mining.view.console.Outputs;


public class InitProgram {

	public static void main(String[] args) 
	{
		
	CommonLogics.assignInitValues(args);
		
	MainController mc = new MainController();

	mc.start();
	

	
	}

}
