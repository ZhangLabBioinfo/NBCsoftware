package com.novelbio.word;

import java.util.Map;

import org.apache.log4j.Logger;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import com.novelbio.word.Document;
import com.novelbio.nbcReport.Params.ReportBase;

public class NBCWord {
	
	public static Logger logger = Logger.getLogger(NBCWord.class);
	
	/**word应用程序*/
	private ActiveXComponent wordApp;
	/**打开的文档集合*/
	private Dispatch dispatchDoc;
	/**当前的文档*/
	private Document nowDoc;
	
	public NBCWord(String path) {
		initWordApp();
		if(path == null)
			addNewDocument();
		else
			openExistDocument(path);
	}
	
	/**
	 * 初始化word，即打开一个word应用
	 */
	private void initWordApp() {
		ComThread.InitSTA();
		wordApp = new ActiveXComponent("Word.Application");
		//Visible 设置是否在桌面打开一个word档
		wordApp.setProperty("Visible", new Variant(true));
		dispatchDoc = wordApp.getProperty("Documents").toDispatch();
	}
	
	/** 打开现有文档 */
	private void openExistDocument(String path) {
		Dispatch dispatch = Dispatch.call(dispatchDoc, "Open", path).toDispatch();
		nowDoc =new Document(wordApp, dispatch, dispatchDoc);
	}

	/** 新建空文档 */
	private void addNewDocument() {
		Dispatch dispatch = Dispatch.call(dispatchDoc, "Add").toDispatch();
		nowDoc = new Document(wordApp, dispatch);
//		dispatchDoc = wordApp.getProperty("Documents").toDispatch();
		nowDoc.setDispatchDoc(dispatchDoc);
	}

	/**
	 * 渲染报告
	 * @param reportBase
	 */
	public void renderReport(ReportBase reportBase){
		nowDoc.render(reportBase.buildFinalParamMap());
	}
	
	/**
	 * 文档另存为
	 * @param filePathName
	 */
	public void saveDocAs(String filePathName) {
		nowDoc.saveAs(filePathName);
	}
	
	/**
	 * 文档保存
	 */
	public void saveDoc() {
		nowDoc.save();
	}
	
	/**
	 * 退出word，在之前要保存
	 */
	public void quit() {
		nowDoc.close();
		wordApp.invoke("Quit", new Variant[]{new Variant(false),new Variant(false)});
		ComThread.Release();
	}

}
