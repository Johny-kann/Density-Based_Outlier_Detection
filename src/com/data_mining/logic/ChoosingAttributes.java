package com.data_mining.logic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.data_mining.constants.FilesList;
import com.data_mining.constants.Notations;
import com.data_mining.file_readers.TextFileWriter;
import com.data_mining.logs.TrainingLog;

import com.data_mining.model.attributes_records.DataTable;
import com.data_mining.model.attributes_records.OrderedClassSet;
import com.data_mining.view.console.Outputs;


/**
 * @author Janakiraman
 *
 *Class with methods to choose the best attributes
 */
public class ChoosingAttributes {

		
	
	
	/**
	 * @param table
	 * @param attribute index
	 * @return possible values of continuous attributes
	 */
	private List<Double> findValuesForContinuousAttributes(DataTable input, int index)
	{
		CommonLogics cl = new CommonLogics();
		
		cl.sort(input, index);
		
		List<Double> splits = cl.fillSplitList(input, index);
		List<Integer> pos = cl.splitPostition(input);
		
		List<Double> values = new ArrayList<Double>();
		for(int i=0;i<pos.size();i++)
		{
			values.add(splits.get(pos.get(i)));
		}
		
		return values;
	}
	
	/**
	 * @param categories
	 * @return error gain
	 */
	public Double error(Map<String,Integer> categories)
	{
		ErrorsAndGain errGain = new ErrorsAndGain();
		return errGain.classificationError(categories);
	}
	
	
	
	
	public double laplaceForTable(DataTable table,String classValue)
	{
		CommonLogics cl = new CommonLogics();
		Integer correctClass = cl.getCountOfClassValue(table, classValue);
		Integer noOfRecords = table.sizeOfRecords();
		Integer classValues = table.getClassValues().size();
		
		ErrorsAndGain error = new ErrorsAndGain();
		return error.laplace(correctClass, noOfRecords, classValues);
		
	}
	
	
	
	
}
