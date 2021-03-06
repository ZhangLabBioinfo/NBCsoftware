package com.novelbio.nbcgui.controltest;

import java.util.Collections;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;

import org.apache.log4j.Logger;

import com.novelbio.analysis.annotation.functiontest.ElimGOFunTest;
import com.novelbio.analysis.annotation.functiontest.FunctionTest;
import com.novelbio.analysis.annotation.functiontest.NovelGOFunTest;
import com.novelbio.analysis.annotation.functiontest.TopGO.GoAlgorithm;
import com.novelbio.base.PathDetail;
import com.novelbio.base.StringOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.GOtype;

public class CtrlGO extends CtrlGOPath {
	private static final Logger logger = Logger.getLogger(CtrlGO.class);
	
	GOtype GOClass = GOtype.BP;
	GoAlgorithm goAlgorithm = GoAlgorithm.novelgo;
	String goAnnoFile;
	boolean isCombine;
	
	int goLevel = -1;
	public GOtype getGOType() {
		return GOClass;
	}
	
	/**
	 * 必须第一时间设定，这个就会初始化检验模块
	 * 如果重新设定了该算法，则所有参数都会清空
	 * @param goAlgorithm
	 */
	public void setGoAlgorithm(GoAlgorithm goAlgorithm) {
		this.goAlgorithm = goAlgorithm;
		if (goAlgorithm != GoAlgorithm.novelgo) {
			functionTest = FunctionTest.getInstance(FunctionTest.FUNCTION_GO_ELIM);
			((ElimGOFunTest) functionTest).setAlgorithm(goAlgorithm);
		} else {
			functionTest = FunctionTest.getInstance(FunctionTest.FUNCTION_GO_NOVELBIO);
		}
	}
	
	public String getTestMethod() {
		if (goAlgorithm != GoAlgorithm.novelgo) {
			return goAlgorithm.name();
		} else {
			return "fisher Test";
		}
	}
	
	
	public GoAlgorithm getGoAlgorithm() {
		return goAlgorithm;
	}
	/** GO的层级分析，只有当算法为NovelGO时才能使用 */
	public void setGOlevel(int levelNum) {
		if (functionTest instanceof NovelGOFunTest) {
			goLevel = levelNum;
			((NovelGOFunTest) functionTest).setGOlevel(levelNum);
		}
	}
	
	/** 设定自定义的GO注释文件
	 * @param goAnnoFile GO注释文件，第一列为GeneName，第二列为GOIterm
	 * @param isCombineDB 是否与数据库已有的数据进行合并，false表示仅用输入的文本来做go分析
	 */
	public void setGOanno(String goAnnoFile, boolean isCombine) {
		((NovelGOFunTest) functionTest).setGoAnnoFile(goAnnoFile, isCombine);
		this.goAnnoFile = goAnnoFile;
		this.isCombine = isCombine;
	}
	
	public void setGOType(GOtype goType) {
		this.GOClass = goType;
		functionTest.setDetailType(goType);
	}

	@Override
	protected void copeFile(String prix, String excelPath) {
		if (goAlgorithm != GoAlgorithm.novelgo) {
			String goMapFileSource = ((ElimGOFunTest)functionTest).getTopGoPdfFile();
			excelPath = FileOperate.changeFilePrefix(excelPath, prix + ".", null);
			String goMapFileTargetName = FileOperate.changeFileSuffix(excelPath, ".GoMap", "pdf");
			FileOperate.moveFile(true, goMapFileSource, goMapFileTargetName);
			
			String goScriptSource = ((ElimGOFunTest)functionTest).getTopGoScript();
			String scriptPath = FileOperate.getPathName(excelPath) + "script/";
			FileOperate.createFolders(scriptPath);
			String scriptName = FileOperate.getFileName(excelPath);
			String goScriptTarget = FileOperate.getPathName(excelPath) + "script/" + scriptName;
			goScriptTarget = FileOperate.changeFileSuffix(goScriptTarget, "." + goAlgorithm, "R");
			FileOperate.moveFile(true, goScriptSource, goScriptTarget);
		}
	}
	
	@Override
	String getGene2ItemFileName(String fileName) {
		String suffix = "_GO_Item";
		List<Integer> blastTaxID = functionTest.getBlastTaxID();
		if (functionTest.isBlast()) {
			suffix = suffix + "_blast";
			Collections.sort(blastTaxID);//排个序
			for (int i : blastTaxID) {
				suffix = suffix + "_" + i;
			}
		}
		suffix = suffix + "_" + GOClass.getOneWord();
		if (goLevel > 0) {
			suffix = suffix + "_" + goLevel + "Level";
		}
		String bgName = FileOperate.changeFileSuffix(fileName, suffix, "txt");
		bgName = FileOperate.changeFilePrefix(bgName, ".", null);
		return bgName;
	}
	
	@Override
	protected void clear() {
		GOClass = GOtype.BP;
		goAlgorithm = GoAlgorithm.classic;
		goLevel = -1;
		functionTest = null;
	}
	
	/** 返回文件的名字，用于excel和画图 */
	public String getResultBaseTitle() {
		return "GO-Analysis_"+getGOType().getTwoWord();
	}
}
