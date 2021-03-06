package com.novelbio.nbcgui.controlquery;

import java.util.List;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.analysis.annotation.blast.BlastNBC;
import com.novelbio.analysis.annotation.blast.BlastStatistics;
import com.novelbio.analysis.annotation.blast.BlastType;
import com.novelbio.analysis.seq.fasta.StrandType;
import com.novelbio.listOperate.HistList;

public class CtrlBlast implements IntCmdSoft {
	BlastNBC blastNBC = new BlastNBC();
	
	/**
	 * 设定blast的模式
	 * @param blastType
	 * BLAST_TBLASTN等
	 */
	public void setBlastType(BlastType blastType) {
		blastNBC.setBlastType(blastType);
	}
	/**
	 * 待query的fasta文件
	 * @param queryFasta
	 */
	public void setQueryFastaFile(String queryFasta) {
		blastNBC.setQueryFastaFile(queryFasta);
	}
	/**
	 * 待比对的数据库，如果是fasta文件，则会自动建索引
	 * @param databaseSeq
	 */
	public void setDatabaseSeq(String databaseSeq) {
		blastNBC.setSubjectSeq(databaseSeq);
	}
	/**
	 * Query strand(s) to search against database/subject. Choice of both, minus, or plus.
	 * 意思比对到reference的正链还是反链。
	 * 所在的blast type：
	 * blastn, blastx, tblastx
	 */
	public void setStrandType(StrandType strandType) {
		blastNBC.setStrandType(strandType);
	}	
	/**
	 * 输出文件
	 * @param resultFile
	 */
	public void setResultFile(String resultFile) {
		blastNBC.setResultFile(resultFile);
	}

	/**
	 * 设定cpu使用数量，感觉设定了没用
	 * 默认为2
	 * @param cpuNum
	 */
	public void setCpuNum(int cpuNum) {
		blastNBC.setCpuNum(cpuNum);
	}

	/**
	 * 常规模式为{@link #ResultType_Normal}
	 * 精简模式为{@link #ResultType_Simple}
	 * 具体看文档
	 * @param resultType 默认为{@link #ResultType_Simple}
	 */
	public void setResultType(int resultType) {
		blastNBC.setResultType(resultType);
	}
	/**  @param resultSeqNum 显示几个结果  */
	public void setResultSeqNum(int resultSeqNum) {
		blastNBC.setResultSeqNum(resultSeqNum);
	}
	/**  输出几个比对结果，当setResultType为8的时候好像不起作用 */
	public void setResultAlignNum(int resultAlignNum) {
		blastNBC.setResultAlignNum(resultAlignNum);
	}
	/** @param evalue 最低相似度，越小相似度越高。最好是0到1之间，默认0.1。一般不用改 */
	public void setEvalue(double evalue) {
		blastNBC.setEvalue(evalue);
	}
	/** query的reads是否特别短 */
	public void setShortQuerySeq(boolean isShortQuerySeq) {
		blastNBC.setShortQuerySeq(isShortQuerySeq);
	}
	
	public void blast() {
		blastNBC.blast();
		if (blastNBC.getResultType() == BlastNBC.ResultType_Simple) {
			BlastStatistics blastStatistics = new BlastStatistics();
			blastStatistics.setBlastResultFile( blastNBC.getResultFile());
			HistList histEvalue = blastStatistics.getHistEvalue();
			HistList histIdentity = blastStatistics.getHistIdentity();
		}
	}
	@Override
	public List<String> getCmdExeStr() {
		return blastNBC.getCmdExeStr();
	}
}
