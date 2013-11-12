package com.novelbio.nbcgui.controlseq;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.HashMultimap;
import com.novelbio.analysis.seq.FormatSeq;
import com.novelbio.analysis.seq.mapping.MapBowtie;
import com.novelbio.analysis.seq.mapping.MapDNA;
import com.novelbio.analysis.seq.mapping.MapDNAint;
import com.novelbio.analysis.seq.mapping.MapLibrary;
import com.novelbio.analysis.seq.sam.SamFileStatistics;
import com.novelbio.base.FoldeCreate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;
import com.novelbio.nbcReport.Params.EnumReport;
import com.novelbio.nbcReport.Params.ReportDNASeqMap;

@Component
@Scope("prototype")
public class CtrlDNAMapping {
	private static final Logger logger = Logger.getLogger(CtrlDNAMapping.class);
	public static final int MAP_TO_CHROM = 8;
	public static final int MAP_TO_REFSEQ = 4;
	public static final int MAP_TO_REFSEQ_LONGEST_ISO = 2;
	
	private String outFilePrefix = "";
	
	private Map<String, List<List<String>>> mapPrefix2LsFastq;
	MapLibrary libraryType = MapLibrary.SingleEnd;
	int gapLen = 5;
	double mismatch = 2;
	int sensitive = MapBowtie.Sensitive_Sensitive;
	int thread = 4;
	boolean isNeedSort = true;
	String chrIndexFile;
	Species species;
	int map2Index = MAP_TO_CHROM;
	
	SoftWare softMapping = SoftWare.bwa;
	
	SoftWareInfo softWareInfo = new SoftWareInfo();
	ReportDNASeqMap reportDNASeqMap = new ReportDNASeqMap();
	
	SamFileStatistics samFileStatistics;
	/** 
	 * @param species
	 * @param map2Index mapping到什么上面去，有chrom，refseq和refseqLongestIso三种
	 */
	public void setSpecies(Species species, int map2Index) {
		this.species = species;
		this.map2Index = map2Index;
	}
	
	public ReportDNASeqMap getReportDNASeqMap() {
		return reportDNASeqMap;
	}
	
	/** 设定输入文件 */
	public void setMapCondition2CombFastQLRFiltered(Map<String, List<List<String>>> mapPrefix2LsLRfq) {
		this.mapPrefix2LsFastq = mapPrefix2LsLRfq;
	}
	
	public void setCopeFastq(CopeFastq copeFastq) {
		copeFastq.setMapCondition2LsFastQLR();
		this.mapPrefix2LsFastq = copeFastq.getMapCondition2LslsFastq();
	}
	
	public SamFileStatistics getSamFileStatistics() {
		return samFileStatistics;
	}

	public void setLibraryType(MapLibrary selectedValue) {
		this.libraryType = selectedValue;
	}
	public void setChrIndexFile(String chrIndexFile) {
		if (FileOperate.isFileExistAndBigThanSize(chrIndexFile, 10)) {
			this.chrIndexFile = chrIndexFile;
		}
	}
	public void setOutFilePrefix(String outFilePrefix) {
		this.outFilePrefix = FoldeCreate.createAndInFold(outFilePrefix, EnumReport.DNASeqMap.getResultFolder());
	}
	public String getOutFilePrefix() {
		return outFilePrefix;
	}
	public void setSortNeed(boolean isNeedSort) {
		this.isNeedSort = isNeedSort;
	}
	public void setGapLen(int gapLen) {
		this.gapLen = gapLen;
	}
	public int getGapLen() {
		return gapLen;
	}
	public void setMismatch(Double mismatch) {
		this.mismatch = mismatch;
	}
	public double getMismatch() {
		return mismatch;
	}
	public void setSensitive(int sensitive) {
		this.sensitive = sensitive;
	}
	public int getSensitive() {
		return sensitive;
	}
	public void setSoftMapping(SoftWare softMapping) {
		this.softMapping = softMapping;
	}
	public SoftWare getSoftMapping() {
		return softMapping;
	}
	public void setThread(int thread) {
		this.thread = thread;
	}
	public int getThread() {
		return thread;
	}
	
	public void running() {
		mapping();
	}
	
	private void mapping() {
		for (Entry<String, List<List<String>>> entry : mapPrefix2LsFastq.entrySet()) {
			List<List<String>> lsFastQs = entry.getValue();
			mapping(entry.getKey(), lsFastQs);
			SamFileStatistics.saveExcel(outFilePrefix + entry.getKey(), samFileStatistics);
			SamFileStatistics.savePic(outFilePrefix + entry.getKey(), samFileStatistics);
		}
	}
	
	/**
	 * 外部调用使用，
	 * 使用方法：<br>
	 * for (Entry<String, FastQ[]> entry : mapCondition2CombFastQLRFiltered.entrySet()) {<br>
			mapping(entry.getKey(), entry.getValue());<br>
		}<br>
	 * <br>
	 *  供AOP使用
	 * @param prefix 文件前缀，实际输出文本为{@link #outFilePrefix} + prefix +.txt
	 * @param fastQs
	 */
	public void mapping(String prefix, List<List<String>> fastQsFile) {
		softWareInfo.setName(softMapping);
		MapDNAint mapSoftware = MapDNA.creatMapDNA(softMapping);		
		mapSoftware.setExePath(softWareInfo.getExePath());

		if (species.getTaxID() == 0) {
			mapSoftware.setChrIndex(chrIndexFile);
		} else {
			if (map2Index == MAP_TO_CHROM) {
				mapSoftware.setChrIndex(species.getIndexChr(softMapping));
			} else if (map2Index == MAP_TO_REFSEQ) {
				mapSoftware.setChrIndex(species.getIndexRef(softMapping, true));
			} else if (map2Index == MAP_TO_REFSEQ_LONGEST_ISO) {
				mapSoftware.setChrIndex(species.getIndexRef(softMapping, false));
			}
		}
		mapSoftware.setLeftFq(CopeFastq.convertFastqFile(fastQsFile.get(0)));
		mapSoftware.setRightFq(CopeFastq.convertFastqFile(fastQsFile.get(1)));
		if (mapSoftware instanceof MapBowtie) {
			((MapBowtie)mapSoftware).setSensitive(sensitive);
		}
		mapSoftware.setPrefix(prefix);
		mapSoftware.setOutFileName(outFilePrefix + prefix);
		mapSoftware.setGapLength(gapLen);
		mapSoftware.setMismatch(mismatch);
		mapSoftware.setSampleGroup(prefix, prefix, prefix, null);
		mapSoftware.setMapLibrary(libraryType);
		mapSoftware.setSortNeed(isNeedSort);
		mapSoftware.setThreadNum(thread);
		samFileStatistics = new SamFileStatistics(prefix);
		mapSoftware.addAlignmentRecorder(samFileStatistics);
		mapSoftware.mapReads();
	}
	
	/**
	 * 预判结果文件<br>
	 * key: 文件类型<br>
	 * value：结果文件名
	 * @return
	 */
	public HashMultimap<String, String> getPredictMapPrefix2Result(List<String> lsPrefix) {
		HashMultimap<String, String> mapFileFormat2FileName = HashMultimap.create();
		softWareInfo.setName(softMapping);
		MapDNAint mapSoftware = MapDNA.creatMapDNA(softMapping);
		for (String prefix : lsPrefix) {
			mapSoftware.setOutFileName(outFilePrefix + prefix);
			mapFileFormat2FileName.put(FormatSeq.BAM.toString(), mapSoftware.getOutNameCope());
			mapFileFormat2FileName.put("Excel", SamFileStatistics.getSaveExcel(outFilePrefix + prefix));
			mapFileFormat2FileName.put("Pic", SamFileStatistics.getSavePic(outFilePrefix + prefix));
		}
		return mapFileFormat2FileName;
	}
	
	public static HashMap<String, Integer> getMapStr2Index() {
		HashMap<String, Integer> mapStr2Index = new HashMap<String, Integer>();
		mapStr2Index.put("chromosome", MAP_TO_CHROM);
		mapStr2Index.put("refseq", MAP_TO_REFSEQ);
		mapStr2Index.put("refseq Longest Iso", MAP_TO_REFSEQ_LONGEST_ISO);
		return mapStr2Index;
	}

}
