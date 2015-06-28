package com.data_mining.controller;

import java.awt.Point;
import java.util.List;
import java.util.Map;

import com.data_mining.constants.FilesList;
import com.data_mining.constants.Notations;
import com.data_mining.constants.ValueConstants;
import com.data_mining.file_readers.PropertiesConfig;
import com.data_mining.file_readers.TextFileWriter;
import com.data_mining.logic.AnomalyLogic;
import com.data_mining.logic.AttributeAndRecordLoaders;
import com.data_mining.logic.ClusterLogics;
import com.data_mining.logic.CommonLogics;
import com.data_mining.logic.ErrorsAndGain;
import com.data_mining.logic.SearchingLogics;
import com.data_mining.logs.TrainingLog;
import com.data_mining.model.attributes_records.DataTable;
import com.data_mining.model.attributes_records.OrderedClassSet;
import com.data_mining.model.clusters.AlgorithmType;
import com.data_mining.model.clusters.CluseterList;
import com.data_mining.model.clusters.Cluster;
import com.data_mining.model.clusters.DataCluster;
import com.data_mining.model.errors.TPRandFPR;
import com.data_mining.view.console.Outputs;
import com.data_mininng.model.anomaly.Points;
import com.sun.prism.impl.TextureResourcePool;


/**
 * @author Janakiraman 
 * 
 * Main Controller for loading test data and train data and calling functions
 *
 */
public class MainController {

	DataTable mainAttributes;
	DataTable trainData;
	DataTable validationData;
	DataTable testData;
	
	OrderedClassSet sortedClassSet;
	DataCluster dataCluster;
	CluseterList clusters;

	
	public MainController()
	{
		mainAttributes = new DataTable();
	
		testData = new DataTable();
		dataCluster = new DataCluster();
		clusters = new CluseterList();
		PropertiesConfig.assignInputFiles();
	
	}
	
	
	
	public void loadTable()
	{
	//	TransactionTable temp = new TransactionTable();
		DataCluster temp = new DataCluster();
		AttributeAndRecordLoaders.loadAttributeFromFile(temp, FilesList.ATTRIBUTES_FILES, FilesList.RECORD_FILES);
		AttributeAndRecordLoaders.loadAttributeFromFile(mainAttributes, FilesList.ATTRIBUTES_FILES, FilesList.RECORD_FILES);
	//	transTable = temp;
		if(Notations.NORMALIZING)
		{
			dataCluster = new ClusterLogics().normalizeData(temp);
		}else
		{
			dataCluster = temp;
		}
		
		String minClass = new CommonLogics().findMinClass(mainAttributes);
		Notations.ANOMALY_CLASS = minClass;
	}
	
	public void start()
	{
		loadTable();
		
		if(Notations.RAND_STATISTICS)
		{
			Notations.DEFAULT_CLASS = new ClusterLogics().getMaxClass(dataCluster.getColumClasses());
		}
		
		if(Notations.ALGORITHM_TYPE==AlgorithmType.BasicKmeans)
			startBasicKMeans();
		
		if(Notations.ALGORITHM_TYPE==AlgorithmType.RelativeDensity)
			startAnomaly();
		
		
	}

	public void startBasicKMeans()
	{
	
	//	loadTable();
	StringBuffer stb = new StringBuffer();
	
	stb.append(Notations.ALGORITHM_TYPE);
	stb.append(System.lineSeparator());
	stb.append("Outlier Estimate : "+Notations.OUTLIER_TYPE);
	stb.append(System.lineSeparator());
		for(int j=3;j<=10;j++)
		{
			
		ValueConstants.K_NUMBER = j;
		stb.append("K = "+j);
		stb.append(System.lineSeparator());
		
		clusters.getClusters().clear();
		
		for(int i = 0 ;i<ValueConstants.K_NUMBER;i++)
		{
			clusters.addCluster(new Cluster(
					new CommonLogics().generateRandomNumbers(1, dataCluster.numberOfPoints()),i)
					);
			
			clusters.getClusterAt(i).setAttributes(dataCluster.getAttributes());
		}
		
		
		
		stb.append("Area of ROC Curve = "+computeClusters());
		stb.append(System.lineSeparator());
		
		}
		
	//	System.out.println(stb.toString());
		
		printResult(stb.toString());
		
	}
	

	
	public String computeClusters()
	{
		ClusterLogics cl = new ClusterLogics();
		AnomalyLogic al = new AnomalyLogic();
		List<Points> points = al.createPointsFromRecords(mainAttributes);
		cl.computeFinalClusters(clusters, dataCluster, points);
		
		al.findDistanceAllClusterToCentroid(points, clusters);
		
		System.out.println(new Outputs().outputClusterListPoints(clusters));
		
		List<TPRandFPR> tprs = al.findRocTPR(points);

		return findArea(tprs);
	}
	

	
	public void startAnomaly()
	{
		StringBuffer stb = new StringBuffer();
		stb.append(Notations.ALGORITHM_TYPE);
		stb.append(System.lineSeparator());
		
		for(int i = 3;i<=10 && i<mainAttributes.sizeOfRecords();i++	)
		{
			ValueConstants.K_NEAREST_NEIGHBOUR = i;
			List<TPRandFPR> points = findTPRsForK();
			
			stb.append("K - Nearest Neighbours "+i);
			stb.append(System.lineSeparator());
			stb.append("Area of ROC Curve " + findArea(points));
			stb.append(System.lineSeparator());
			stb.append(System.lineSeparator());
			
		}
		
		System.out.println(stb.toString());
		printResult(stb.toString());
	}
	
	public List<TPRandFPR> findTPRsForK()
	{
		AnomalyLogic anomaly = new AnomalyLogic();
		
		List<Points> point = anomaly.findAllNearestNeighbors(this.mainAttributes);
	
		anomaly.computeAllOutliers(point);
		
		List<TPRandFPR> list = anomaly.findRocTPR(point);
		
		return list;
		
	}
	
	public String findArea(List<TPRandFPR> points)
	{
		AnomalyLogic anomaly = new AnomalyLogic();
		Double area = new ErrorsAndGain().roundOff(anomaly.findAreaRoc(points
				),5);
		
		return area+"";
		
	}
	
	public void printResult(String content)
	{
		new TextFileWriter(FilesList.WRITE_RESULT).writeFile(content, FilesList.WRITE_RESULT);
	}
	

	
	public String outputClusters()
	{
		StringBuffer stb = new StringBuffer();
		stb.append(Notations.ALGORITHM_TYPE);
		stb.append(System.lineSeparator());
		stb.append("Table used ");
		stb.append(System.lineSeparator());
		stb.append(new Outputs().outPutTable(dataCluster));
		stb.append(System.lineSeparator());
		stb.append("Clusters Formed ");
		stb.append(new Outputs().outputClusterList(clusters));
		stb.append(System.lineSeparator());
		
		if(Notations.SILHOUTTE)
		{
			double err = new ErrorsAndGain().calculateSilhoute(clusters);
			stb.append("Silhoutte error "+err);
			stb.append(System.lineSeparator());
		}if(Notations.RAND_STATISTICS)
		{
			new ClusterLogics().assignClusterClass(clusters);
			
			double err2 = new ErrorsAndGain().RandStatistic(new ClusterLogics().computeRandPoints(clusters));
			stb.append("Rand Statistics "+err2);
			stb.append(System.lineSeparator());
		}
		
		return stb.toString();
		
	}
	
	
	
	public DataTable getMainTable()
	{
		return mainAttributes;
	}
	
	public DataTable getTrainAttributes()
	{
		return trainData;
	}
	
	public DataTable getTestAttributes() {
		return validationData;
	}

	public OrderedClassSet getSortedClassSet() {
		return sortedClassSet;
	}



	public DataCluster getDataCluster() {
		return dataCluster;
	}



	public CluseterList getClusters() {
		return clusters;
	}
	
	
}
