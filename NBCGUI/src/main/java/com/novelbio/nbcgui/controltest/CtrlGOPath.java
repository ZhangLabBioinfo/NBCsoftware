package com.novelbio.nbcgui.controltest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.novelbio.analysis.annotation.functiontest.FunctionTest;
import com.novelbio.analysis.annotation.functiontest.FunctionTest.FunctionDrawResult;
import com.novelbio.analysis.annotation.functiontest.StatisticTestResult;
import com.novelbio.analysis.seq.genome.GffSpeciesInfo;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.ExcelStyle;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.ExceptionNbcFile;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.domain.geneanno.EnumSpeciesFile;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.species.Species;
/**
 * 考虑添加进度条
 * @author zong0jie
 */

public abstract class CtrlGOPath extends RunProcess {
	private static final Logger logger = Logger.getLogger(CtrlGOPath.class);
	public static final String All = "All";
	public static final String Up = "Up";
	public static final String Down = "Down";
	
	int limitGeneNum = 0;
	
	FunctionTest functionTest = null;
	
	double up = -1;
	int upGeneNum = -100, downGeneNum = -100, allGeneNum = -100;
	
	double down = -1;
	
	/** 是否为clusterGO */
	boolean isCluster = false;
	
	List<String> lsResultExcel = new ArrayList<>();
	/** 
	 * 读入的gene2Value表
	 * lsAccID2Value  arraylist-string[] 若为string[2],则第二个为上下调关系，判断上下调
	 * 若为string[1] 则跑全部基因作分析
	 */
	List<String[]> lsAccID2Value;
//	Species species;

	Map<String, FunctionDrawResult> mapPrefix2FunDrawTest = new LinkedHashMap<String, FunctionDrawResult>();

	String bgFile = "";
	private String saveExcelPrefix;
	
	String gene2itemAnnoFile;
	/** true表示与数据库的注释合并，false表示仅用该注释文件进行go注释 */
	boolean isCombine = true;
	
	/** 输入的是accId还是GeneId */
	boolean isGeneId = false;
	
	public void setTaxID(int taxId) {
		functionTest.setTaxID(taxId);
	}
	public void setIsGeneId(boolean isGeneId) {
		this.isGeneId = isGeneId;
	}
	public int getTaxID() {
		return functionTest.getTaxID();
	}
	public List<Integer> getBlastTaxID() {
		return functionTest.getBlastTaxID();
	}
	public boolean isCluster() {
		return isCluster;
	}
	/** lsAccID2Value  arraylist-string[] 若为string[2],则第二个为上下调关系，判断上下调
	 * 若为string[1] 则跑全部基因作分析
	 */
	public void setLsAccID2Value(List<String[]> lsAccID2Value) {
		this.lsAccID2Value = lsAccID2Value;
	}
	
	public void setUpDown(double up, double down) {
		this.up = up;
		this.down = down;
	}
	
	public void setBlastInfo(double blastevalue, List<Integer> lsBlastTaxID) {
		functionTest.setBlastInfo(blastevalue, lsBlastTaxID);
	}
	public void setGene2itemAnnoFile(String gene2itemAnnoFile, boolean isCombine) {
		if (!FileOperate.isFileExistAndBigThanSize(gene2itemAnnoFile, 0)) {
			throw new ExceptionNbcFile("gene to item file " + gene2itemAnnoFile + " is not exist");
		}
		this.gene2itemAnnoFile = gene2itemAnnoFile;
		this.isCombine = isCombine;
	}
	/**
	 * 必须设定<br>
	 * <b>在这之前要先设定GOlevel</b><br>
	 * 如果输入的是空值，就会自动设定BG
	 * 
	 * 简单的判断下输入的是geneID还是geneID2Item表
	 * @param fileName
	 */
	public void setLsBG(String fileName) {
		this.bgFile = fileName;
	}
	/**
	 * 必须设定<br>
	 * <b>在这之前要先设定GOlevel</b><br>
	 * 如果输入的是空值，就会自动设定BG
	 * 
	 * 简单的判断下输入的是geneID还是geneID2Item表
	 * @param fileName
	 */
	public void setLsBG(Species species) {
		String fileName = EnumSpeciesFile.bgGeneFile.getSavePath(species.getTaxID(), species.getSelectSpeciesFile());
		if (!FileOperate.isFileExistAndBigThanSize(fileName, 0)) {
			GffSpeciesInfo specieInformation = new GffSpeciesInfo();
			specieInformation.setSpecies(species);
			specieInformation.writeGeneBG(fileName);
		}
		this.bgFile = fileName;
	}
	
	private void setBG() {
		if (!isCombine && FileOperate.isFileExistAndBigThanSize(gene2itemAnnoFile, 0)) {
			functionTest.readGene2ItemAnnoFile(gene2itemAnnoFile);
			return;
		}
		
		boolean isGene2LsItem = testBGfile(bgFile);
		if (isGene2LsItem) {
			functionTest.setLsBGItem(bgFile);
		} else {
			if (FileOperate.isFileExistAndBigThanSize( getGene2ItemFileName(bgFile), 10)) {
				functionTest.setLsBGItem(getGene2ItemFileName(bgFile));
			} else {
				functionTest.setLsBGAccID(bgFile, 1, getGene2ItemFileName(bgFile));
			}
		}
		
		if (FileOperate.isFileExistAndBigThanSize(gene2itemAnnoFile, 0)) {
			functionTest.readGene2ItemAnnoFile(gene2itemAnnoFile);
		}
	}
	/**
	 * 文件名后加上go_item或者path_item等
	 * @param fileName
	 * @return
	 */
	abstract String getGene2ItemFileName(String  fileName);
	/**
	 * 测试文件是否为gene item,item的格式
	 * @param fileName
	 * @return
	 */
	private boolean testBGfile(String fileName) {
		boolean result = false;
		TxtReadandWrite txtRead = new TxtReadandWrite(fileName);
		int i  = 0;
		for (String content : txtRead.readlines()) {
			if (content.startsWith("#")) {
				continue;
			}
			String[] ss = content.split("\t");
			//TODO 判定是否为gene item,item的格式
			if (ss.length == 2 && ss[1].contains(",") && ss[1].split(",")[0].contains(":")) {
				result = true;
				break;
			}
			if (i++ > 100) {
				break;
			}
		}
		txtRead.close();
		return result;
	}
	
	public void setIsCluster(boolean isCluster) {
		this.isCluster = isCluster;
	}
	
	public Map<String, FunctionDrawResult> getMapPrefix2FunDrawTest() {
		return mapPrefix2FunDrawTest;
	}
	
	/**
	 * 获得上下调数
	 * @return 0 上调数 1 下调数 2 总和
	 */
	public int[] getUpAndDownRegulation() {
		return new int[]{upGeneNum,downGeneNum,allGeneNum};
	}
	
	public void running() {
		setBG();
		if (isCluster) {
			runCluster();
		} else {
			runNorm();
		}
	}
	
	public void setSaveExcelPrefix(String saveExcelPrefix) {
		this.saveExcelPrefix = saveExcelPrefix;
	}
	
	/** 返回文件的名字，用于excel和画图 */
	public abstract String getResultBaseTitle();
	
	/**
	 * 给定文件，和文件分割符，以及第几列，获得该列的基因ID
	 * @param lsAccID2Value  arraylist-string[] 如果 string[2],则第二个为上下调关系，判断上下调
	 * 如果string[1]则不判断上下调
	 * @param up
	 * @param down
	 */
	private void runNorm() {
		isCluster = false;
		HashMultimap<String, String> mapPrefix2AccID = HashMultimap.create();
		for (String[] strings : lsAccID2Value) {
			if (strings[0] == null || strings[0].trim().equals("")) {
				continue;
			}
			try {
				if (strings.length == 1) {
					mapPrefix2AccID.put(All, strings[0]);
				} else if (strings.length > 1 && getDoubleValue(strings[1]) >= up) {
					mapPrefix2AccID.put(Up, strings[0]);
					mapPrefix2AccID.put(All, strings[0]);
				} else if (strings.length > 1 && getDoubleValue(strings[1]) <= down) {
					mapPrefix2AccID.put(Down, strings[0]);
					mapPrefix2AccID.put(All, strings[0]);
				}
			} catch (Exception e) {

			}

		}
		HashMultimap<String, GeneID> mapPrefix2SetAccID = addBG_And_Convert2GeneID(mapPrefix2AccID);
		setGeneNum(mapPrefix2AccID);
		
		Set<String> setResultFile = new HashSet<>();
		
		ExcelOperate excelResult = new ExcelOperate(saveExcelPrefix);
		excelResult.setWriteSheetToTxt(true);
		String excelAllPath = FileOperate.changeFileSuffix(saveExcelPrefix, "_All",null);
		ExcelOperate excelResultAll = null;
		if (mapPrefix2SetAccID.keySet().contains(All) && mapPrefix2SetAccID.size() > 0) {
			excelResultAll = new ExcelOperate(excelAllPath);
			excelResultAll.setWriteSheetToTxt(true);
		}
		for (String prefix : mapPrefix2SetAccID.keySet()) {
			FunctionTest functionTest = getResult(prefix, mapPrefix2SetAccID.get(prefix));
			if (functionTest == null) {
				TxtReadandWrite txtWrite = new TxtReadandWrite(FileOperate.changeFileSuffix(saveExcelPrefix, "_NoResult", null), true);
				txtWrite.close();
				continue;
			}
			if (prefix.equals(All) && excelResultAll != null) {
				setResultFile.add(excelAllPath);
				saveExcelNorm(prefix, functionTest, excelResultAll);
			} else {
				setResultFile.add(saveExcelPrefix);
				saveExcelNorm(prefix, functionTest, excelResult);
			}
			copeFile(prefix, saveExcelPrefix);
			mapPrefix2FunDrawTest.put(prefix, functionTest.getFunctionDrawResult());
		}
		excelResult.close();
		if (excelResultAll != null) excelResultAll.close();
		
		lsResultExcel.addAll(setResultFile);
	}
	
	private Double getDoubleValue(String valueStr) {
		Double value = null;
		if (valueStr.toLowerCase().startsWith("inf")) {
			return Double.MAX_VALUE;
		} else if (valueStr.toLowerCase().startsWith("-inf")) {
			return Double.MIN_VALUE;
		} else {
			try {
				value = Double.parseDouble(valueStr);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return value;
	}
	
	private<T> void setGeneNum(HashMultimap<String, T> mapPrefix2Gene) {
		for (String type : mapPrefix2Gene.keySet()) {
			if (type.equalsIgnoreCase(All)) {
				allGeneNum = mapPrefix2Gene.get(All).size();
			} else if (type.equalsIgnoreCase(Up) ) {
				upGeneNum = mapPrefix2Gene.get(Up).size();
			} else if (type.equalsIgnoreCase(Down)) {
				downGeneNum = mapPrefix2Gene.get(Down).size();
			}
		}
	}
	
	/**
	 * 给定文件，和文件分割符，以及第几列，获得该列的基因ID
	 * 
	 * @param showMessage
	 * @return
	 * @throws Exception
	 */
	private void runCluster() {
		isCluster = true;
		HashMultimap<String, String> mapCluster2SetAccID = HashMultimap.create();
		for (String[] accID2prefix : lsAccID2Value) {
			mapCluster2SetAccID.put(accID2prefix[1], accID2prefix[0]);
		}
		HashMultimap<String, GeneID> mapCluster2SetGeneID = addBG_And_Convert2GeneID(mapCluster2SetAccID);
		for (String prefix : mapCluster2SetGeneID.keySet()) {
			FunctionTest functionTest = getResult(prefix, mapCluster2SetGeneID.get(prefix));
			String excelPathOut = FileOperate.changeFilePrefix(saveExcelPrefix, prefix + ".", null);

			if (functionTest == null) {
				TxtReadandWrite txtWrite = new TxtReadandWrite(FileOperate.changeFileSuffix(excelPathOut, "_NoResult", null), true);
				txtWrite.close();
				continue;
			}
			ExcelOperate excelResult = new ExcelOperate(excelPathOut);
			excelResult.setWriteSheetToTxt(true);
			saveExcelCluster(prefix, functionTest, excelResult);
			excelResult.close();
			copeFile(prefix, saveExcelPrefix);
			lsResultExcel.add(excelPathOut);
			mapPrefix2FunDrawTest.put(prefix, functionTest.getFunctionDrawResult());
		}
	}
	
	/** 将输入转化为geneID */
	protected HashMultimap<String, GeneID> addBG_And_Convert2GeneID(HashMultimap<String, String> mapPrefix2SetAccID) {
		HashMultimap<String, GeneID> mapPrefix2SetGeneID = HashMultimap.create();
		for (String prefix : mapPrefix2SetAccID.keySet()) {
			Set<String> setAccID = mapPrefix2SetAccID.get(prefix);
			for (String accID : setAccID) {
				if (StringOperate.isRealNull(accID)) continue;
				
				GeneID geneID = null;
				if (isGeneId) {
					geneID = new GeneID(GeneID.IDTYPE_GENEID, accID, functionTest.getTaxID());
					if (geneID.getAccID_With_DefaultDB() != null || geneID.getLsBlastGeneID().size() > 0 || functionTest.isContainGeneName(geneID.getGeneUniID())) {
						mapPrefix2SetGeneID.put(prefix, geneID);
					}
				} else {
					geneID = new GeneID(accID, functionTest.getTaxID());
					if (geneID.getIDtype() != GeneID.IDTYPE_ACCID || geneID.getLsBlastGeneID().size() > 0 || functionTest.isContainGeneName(geneID.getGeneUniID())) {
						mapPrefix2SetGeneID.put(prefix, geneID);
					}
				}

			}
		}//*1
		//以下是打算将输入的testID补充进入BG，不过我觉得没必要了
		//我sfesa们只要将BG尽可能做到全面即可，不用想太多
//		for (String prefix : mapPrefix2SetGeneID.keySet()) {
//			Set<GeneID> setGeneIDs = mapPrefix2SetGeneID.get(prefix);
//			functionTest.addBGGeneID(setGeneIDs);
//		}
		return mapPrefix2SetGeneID;
	}
	/**
	 * 用这个计算，算完后才能save等
	 * @param functionTest
	 * @param prixz1
	 * @param lsCopedIDs
	 * @return
	 * 没有就返回null   
	 */
	private FunctionTest getResult(String prix, Collection<GeneID>lsCopedIDs) {
		if (limitGeneNum > 100 && lsCopedIDs.size() > limitGeneNum) {
			throw new RuntimeException("GOPath condition: " + prix + " contains " + lsCopedIDs.size() + " genes, GO Pathway Error, cannot calculate so much gene");
		}
		functionTest.setLsTestGeneID(lsCopedIDs);
		ArrayList<StatisticTestResult> lsResultTest = functionTest.getTestResult();
		if (lsResultTest == null || lsResultTest.size() == 0) {
			return null;
		}
		return functionTest.clone();
	}
	
	/** 返回 保存的路径，注意如果是cluster，则返回的是前缀 */
	public String getSaveExcelPrefix() {
		return saveExcelPrefix;
	}
	
	/** 返回保存的文件名 */
	protected void saveExcelNorm(String prefix, FunctionTest functionTest, ExcelOperate excelResult) {
		Map<String,   List<String[]>> mapSheetName2LsInfo = functionTest.getMapWriteToExcel();
		Map<String, Integer> mapSheetName2EndLine = functionTest.getMapSheetName2EndLine();

		for (String sheetName : mapSheetName2LsInfo.keySet()) {
			int endRowNum = mapSheetName2EndLine.get(sheetName) + 1;
			ExcelStyle style = null;
			if (endRowNum > 0) {
				style = ExcelStyle.getThreeLineTable(1, endRowNum);
			}
			excelResult.writeExcel(prefix + sheetName, 1, 1, mapSheetName2LsInfo.get(sheetName),style);
		}
	}
		
//	/**
//	 * 统计结果，返回筛选条件
//	 * 
//	 * @param lsTestResults
//	 * @param knownCondition
//	 *            已知条件，用来比较返回更宽松的条件
//	 * @return
//	 */
//	public String getFinderCondition() {
//		int fdrSum1 = 0;
//		int fdrSum5 = 0;
//		int pValueSum1 = 0;
////		String[] result = { "FDR&lt;0.01", "FDR&lt;0.05", "P-value&lt;0.01", "P-value&lt;0.05" };
//		String[] result = { "FDR<0.01", "FDR<0.05", "P-value<0.01", "P-value<0.05" };
//		if (mapPrefix2FunTest.size() == 0) {
//			return result[1];
//		}
//		FunctionTest functionTest = mapPrefix2FunTest.values().iterator().next();
//		for (StatisticTestResult testResult : functionTest.getTestResult()) {
//			if (testResult.getPvalue() < 0.01) {
//				pValueSum1++;
//			}
//			if (testResult.getPvalue() < 0.05) {
//			}
//			if (testResult.getFdr() < 0.01) {
//				fdrSum1++;
//			}
//			if (testResult.getFdr() < 0.05) {
//				fdrSum5++;
//			}
//		}
//		int currentConditionNum = fdrSum1 > 8 ? 0 : (fdrSum5 > 8 ? 1 : (pValueSum1 > 8 ? 2 : 3));
//		return result[currentConditionNum];
//	}
		
	protected void saveExcelCluster(String prefix, FunctionTest functionTest, ExcelOperate excelResult) {
		Map<String, Integer> mapSheetName2EndLine = functionTest.getMapSheetName2EndLine();
		Map<String, List<String[]>> mapSheetName2LsInfo = functionTest.getMapWriteToExcel();
		for (String sheetName : mapSheetName2LsInfo.keySet()) {
			int endRowNum = mapSheetName2EndLine.get(sheetName) + 1;
			ExcelStyle style = null;
			if (endRowNum > 0) {
				style = ExcelStyle.getThreeLineTable(1, endRowNum);
			}
			excelResult.writeExcel(sheetName, 1, 1, mapSheetName2LsInfo.get(sheetName), style);
		}
	}
	
	public List<String> getLsResultExcel() {
		return lsResultExcel;
	}
	
	/**
	 * 保存文件时，是否需要额外的处理文件，不需要就留空
	 * 譬如elimGO需要移动GOMAP等
	 */
	protected abstract void copeFile(String prix, String excelPath);
	
	/** 清空参数，每次调用之前先清空参数 */
	public void clearParam() {
		up = -1;
		down = -1;
		isCluster = false;
		lsAccID2Value = null;
		clear();
	}
	
	protected abstract void clear();
	
//	/** 根据指定的字符串，返回xdoc的枚举 */
//	public static EnumTableType getXdocGoPath(String sheetName) {
//		if (sheetName.contains(StatisticTestResult.titleGO)) {
//			return EnumTableType.GO_Result;
//		} else if (sheetName.contains(StatisticTestResult.titlePath)) {
//			return EnumTableType.Pathway_Result;
//		} else if (sheetName.contains(StatisticTestGene2Item.titleGO)) {
//			return EnumTableType.GO_Gene2GO;
//		} else if (sheetName.contains(StatisticTestItem2Gene.titleGO)) {
//			return EnumTableType.GO_GO2Gene;
//		} else if (sheetName.contains(StatisticTestGene2Item.titlePath)) {
//			return EnumTableType.Pathway_Gene2Path;
//		}
//		return null;
//	}
	
}

class GoPathInfo {
	int num = 0;
	public GoPathInfo(int num) {
		this.num = num;
	}
}
