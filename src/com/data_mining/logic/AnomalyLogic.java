package com.data_mining.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;



import com.data_mining.constants.FilesList;
import com.data_mining.constants.NodeConstants;
import com.data_mining.constants.Notations;
import com.data_mining.constants.ValueConstants;
import com.data_mining.model.attributes_records.DataTable;
import com.data_mining.model.clusters.CluseterList;
import com.data_mining.model.clusters.Cluster;
import com.data_mining.model.clusters.DataCluster;
import com.data_mining.model.errors.NearestNeighbour;
import com.data_mining.model.errors.TPRandFPR;
import com.data_mininng.model.anomaly.OutlierType;
import com.data_mininng.model.anomaly.Points;

public class AnomalyLogic {

	public void findNearestNeighbour(Points point, int index, List<Points> table)
	{
		List<NearestNeighbour> list = new ArrayList<NearestNeighbour>();
		ClusterLogics cl = new ClusterLogics();
		
		
		for(int i=0;i<table.size();i++)
		{
			
			if(i!=index)
			{
				NearestNeighbour nr =new NearestNeighbour();
			
				nr.setDistance(	
			
					cl.findEucleideanDistance(point.getRecordPoints(),
					table.get(i).getRecordPoints()));
				
				nr.setIndex(i);
				
				list.add(nr);
			
			}
		}
		
		new CommonLogics().sort(list);
		
		for(int i=0;i<ValueConstants.K_NEAREST_NEIGHBOUR && i<list.size();i++)
		{	
		//	Points pts = new Points(table.getAttributes());
		
//		pts.setPoint(table.getRecordAtIndex(list.get(i).getIndex()));
			
			point.addNeighbour(
					table.get(
							list.get(i).getIndex()
							
			//		table.getRecordAtIndex(
				//		list.get(i).getIndex()
						));
			
		}
		
	}

	/**
	 * Assigns the distance of the points from its centroid to all cluster
	 * @param listPoints
	 * @param listCluster
	 */
	public void findDistanceAllClusterToCentroid(List<Points> listPoints,CluseterList listCluster)
	{
		for(int i=0;i<listCluster.size();i++)
		{
			findDistanceToCentroid(listCluster.getClusterAt(i));
		}
	}
	
	
	
	public void findDistanceToCentroid(Cluster cluster)
	{
		ClusterLogics cl = new ClusterLogics();
		
		for(int i=0;i<cluster.size();i++)
		{
			cluster.getPoints().get(i).setOutlierScore(
					cl.findEucleideanDistance
						(cluster.getCentroid().getAttrList(),cluster.getRecordPointsAt(i))
			);
			
		}
		
		if(Notations.OUTLIER_TYPE == OutlierType.relativedistance)
		{
		
			for(int i=0 ; i<cluster.size() ; i++)
		{
			Double median = findMedianDistance(cluster);
			
			cluster.getPoints().get(i).setOutlierScore(
					cluster.getPoints().get(i).getOutlierScore()/median);
			
			if(Double.isNaN(cluster.getPoints().get(i).getOutlierScore()))
			{	
				cluster.getPoints().get(i).setOutlierScore(0.0);
			}
					
		}
			
		}
	}
	
	public Double findMedianDistance(Cluster cluster)
	{
		List<Double> array = new ArrayList<>();
		
		for(int i=0;i<cluster.size();i++)
		{
			array.add(cluster.getPoints().get(i).getOutlierScore());
		}
		
		Collections.sort(array);
		
		if(array.size()%2==0)
		{
			int mid1 = array.size()/2-1;
			int mid2 = mid1+1;
			return ((array.get(mid1)+array.get(mid2))/2
					);
		}
		else
		{
			int mid1 = array.size()/2;
			return array.get(mid1);
		}
	}
	
	public void findingDensity(Points points)
	{
		Double sum = 0.0;
		ClusterLogics cl = new ClusterLogics();
		
		for(int i=0;i<points.nearestNeighbours().size();i++)
		{
			sum += cl.findEucleideanDistance(points.getRecordPoints(), points.getNearestPointAt(i)
					);
		}
		
		double avgDist = sum/points.nearestNeighbours().size();
		
		points.setDensity(
				new ErrorsAndGain().roundOff(1/avgDist, 4)
				);
				
	}

	public List<Points> createPointsFromRecords(DataTable data)
	{
		List<Points> points = new ArrayList<>();
		
		for(int i=0;i<data.sizeOfRecords();i++)
		{
			Points point = new Points(data.getAttributes());
			point.setPoint(data.getRecordAtIndex(i));
			
			points.add(point);
		}
		
		return points;
	}
	
	public List<Points> findAllNearestNeighbors(DataTable table)
	{
		List<Points> points = new ArrayList<>();
		
		for(int i=0;i<table.sizeOfRecords();i++)
		{
			Points point = new Points(table.getAttributes());
			point.setPoint(table.getRecordAtIndex(i));
			
			
			
			points.add(point);
		}
		
		for(int i=0;i<points.size();i++)
		{
			findNearestNeighbour(points.get(i), i, points);
			findingDensity(points.get(i)
					);
			
		}
		
		return points;
	}
	
	public void computeAllOutliers(List<Points> listPoints)
	{
		for(int i = 0 ; i<listPoints.size() ; i++)
		{
			computeOutlierScore(listPoints.get(i));
		}
	}
	
	public void computeOutlierScore(Points point)
	{
		Double sum = 0.0;
		
		for(int i=0;i<point.nearestNeighbours().size();i++)
		{
			sum += point.nearestNeighbours().get(i).getDensity();
		}
		
		point.setOutlierScore(
				 new ErrorsAndGain().roundOff((sum/point.nearestNeighbours().size())/point.getDensity(),4)
				);
		
		
		
	}
	
	public List<TPRandFPR> findRocTPR(List<Points> points)
	{
		List<Double> doubles = new ArrayList<>();
		
		for(int i = 0;i<points.size();i++)
		{
			doubles.add(points.get(i).getOutlierScore());
		}
		
		ValueConstants.OUTLIER_MAX = Collections.max(doubles)+0.3;
		ValueConstants.OUTLIER_MIN = Collections.min(doubles)-0.3;
		
		List<TPRandFPR> list = new ArrayList<>();
		
		for(double i=ValueConstants.OUTLIER_MIN; i<ValueConstants.OUTLIER_MAX; i+=ValueConstants.OUTLIER_STEP_SIZE)
		{
			list.add(findTPR(points, i));
		}
		
		return list;
	}
	
	public Double findAreaRoc(List<TPRandFPR> listTRPs)
	{
		Double area = 0.0;
		Double b,h,l;
		
		for(int i=1;i<listTRPs.size();i++)
		{
			b = listTRPs.get(i).getFPR() - listTRPs.get(i-1).getFPR();
			h = listTRPs.get(i).getTPR();
			l = listTRPs.get(i-1).getTPR();
			area += l*b + (b*(h-l))/2;
		}
		
		return area;
		
	}
	
	public TPRandFPR findTPR(List<Points> points,Double outlierScore)
	{
		int TP = 0;
		int FP = 0;
		int FN = 0;
		int TN = 0;
		
		for(Points point:points)
		{
			if(!point.getPoint().getClassAttribute().equalsIgnoreCase(Notations.ANOMALY_CLASS)
					&& point.getOutlierScore()<outlierScore)
				TP++;
			
			else if(point.getPoint().getClassAttribute().equalsIgnoreCase(Notations.ANOMALY_CLASS) 
					&& point.getOutlierScore()>=outlierScore)
				TN++;
			
			else if(!point.getPoint().getClassAttribute().equalsIgnoreCase(Notations.ANOMALY_CLASS) 
					&& point.getOutlierScore()>=outlierScore)
				FN++;
			
			else if(point.getPoint().getClassAttribute().equalsIgnoreCase(Notations.ANOMALY_CLASS) 
					&& point.getOutlierScore()<outlierScore)
				FP++;
				
		}
		
		TPRandFPR tpr = new TPRandFPR();
		
		tpr.setTPR(
				(double)TP/(TP+FN)
				);
		tpr.setFPR(
				(double)FP/(TN+FP)
				);
		
		return tpr;
	}
}
