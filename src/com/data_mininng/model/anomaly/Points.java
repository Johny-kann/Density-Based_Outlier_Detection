package com.data_mininng.model.anomaly;

import java.util.ArrayList;
import java.util.List;

import com.data_mining.logic.ClusterLogics;
import com.data_mining.model.attributes_records.AttributesSpecifications;
import com.data_mining.model.attributes_records.DataTable;
import com.data_mining.model.attributes_records.Records;
import com.data_mining.model.clusters.AttributeCluster;

public class Points {

	private List<AttributesSpecifications> attrbs;
	private Records point;
	
//	private List<Records> nearestNeighbour;
	private List<Points> nearestNeighbour;
	
	private Double density;
	private Double outlierScore;
	
	public Points(List<AttributesSpecifications> attrb)
	{
	//	nearestNeighbour = new ArrayList<Records>();
		nearestNeighbour = new ArrayList<>();
		this.attrbs = attrb;
	}
	
		
	public Records getPoint() {
		return point;
	}
	
	public void setPoint(Records point) {
		this.point = point;
	}

	public void addNeighbour(Points point)
	{
	//	this.nearestNeighbour.add(point);
		this.nearestNeighbour.add(point);
	}
	
	public List<Points> nearestNeighbours()
	{
		return nearestNeighbour;
	}
	
	public String[] getRecordPoints()
	{
		return new ClusterLogics().getPoints(attrbs, point);
	}
	
	public String[] getNearestPointAt(int index)
	{
		return nearestNeighbour.get(index).getRecordPoints();
			//	new ClusterLogics().getPoints(attrbs, nearestNeighbour.get(index).getRecordPoints()
			//	);
	}

	public Double getDensity() {
		return density;
	}

	public void setDensity(Double density) {
		this.density = density;
	}

	public Double getOutlierScore() {
		return outlierScore;
	}

	public void setOutlierScore(Double outlierScore) {
		this.outlierScore = outlierScore;
	}
	
}
