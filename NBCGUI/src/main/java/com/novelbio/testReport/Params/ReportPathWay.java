package com.novelbio.testReport.Params;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.novelbio.database.model.species.Species;
import com.novelbio.nbcReport.XdocTmpltExcel;
import com.novelbio.nbcReport.XdocTmpltPic;

public class ReportPathWay extends ReportBase{
	
	public ReportPathWay() {
		// TODO Auto-generated constructor stub
	}
	
	/**设置物种*/
	public void setSpecies(Species species) {
		String name = species.getNameLatin();
		String[] ss = name.split(" ");
		if (ss.length > 2) {
			name = ss[0] + " " + ss[1];
		}
		mapKey2Param.put("SpeciesName", name);
	}

	/** 实验组名 */
	public void setTeamName(String teamName) {
		mapKey2Param.put("teamName", teamName);
	}

	/** 所使用的数据库 */
	public void setDb(String db) {
		mapKey2Param.put("database", db);
	}

	/** 筛选条件 */
	public void setFinderCondition(String finderCondition) {
		mapKey2Param.put("finderCondition", finderCondition);
	}

	public void setUpRegulation(int upRegulation) {
		mapKey2Param.put("upRegulation", upRegulation);
	}

	public void setDownRegulation(int downRegulation) {
		mapKey2Param.put("downRegulation", downRegulation);
	}

	@Override
	public EnumReport getEnumReport() {
		return EnumReport.PathWay;
	}

}
