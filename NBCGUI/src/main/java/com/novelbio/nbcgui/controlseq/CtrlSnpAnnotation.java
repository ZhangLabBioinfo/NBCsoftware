package com.novelbio.nbcgui.controlseq;

import javax.swing.JOptionPane;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.resequencing.SnpAnnotation;
import com.novelbio.analysis.seq.resequencing.SnpFilterDetailInfo;
import com.novelbio.base.multithread.RunGetInfo;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.nbcgui.GUI.GuiSnpCalling;
import com.novelbio.nbcgui.GUI.GuiSnpCallingInt;

public class CtrlSnpAnnotation implements RunGetInfo<SnpFilterDetailInfo> {

	SnpAnnotation snpAnnotation = new SnpAnnotation();
	GuiSnpCallingInt guiSnpCalling;

	public CtrlSnpAnnotation(GuiSnpCallingInt guiSnpCalling) {
		this.guiSnpCalling = guiSnpCalling;
		snpAnnotation.setRunGetInfo(this);
	}

	public CtrlSnpAnnotation() {
		snpAnnotation.setRunGetInfo(this);
	}

	public void setProcessBar() {
		guiSnpCalling.getProgressBar().setMaximum((int) snpAnnotation.getFileSizeEvaluateK());
	}

	public void clean() {
		snpAnnotation.clearSnpFile();
	}

	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		snpAnnotation.setGffChrAbs(gffChrAbs);
	}

	public void setCol(int colChrID, int colRefStartSite, int colRefNr, int colThisNr) {
		snpAnnotation.setCol(colChrID, colRefStartSite, colRefNr, colThisNr);
	}

	public void addSnpFile(String txtFile, String txtOut) {
		snpAnnotation.addTxtSnpFile(txtFile, txtOut);
	}

	public void runAnnotation() {
		if (guiSnpCalling != null) {
			guiSnpCalling.getBtnAddPileupFile().setEnabled(false);
			guiSnpCalling.getBtnDeletePileup().setEnabled(false);
			guiSnpCalling.getBtnRun().setEnabled(false);
			guiSnpCalling.getProgressBar().setMinimum(0);
			guiSnpCalling.getProgressBar().setMaximum((int) snpAnnotation.getFileSizeEvaluateK());
			
			Thread thread = new Thread(snpAnnotation);
			thread.setDaemon(true);
			thread.start();
		} else {
			snpAnnotation.run();
		}
		

	}

	@Override
	public void setRunningInfo(SnpFilterDetailInfo info) {
		if (guiSnpCalling != null) {
			long kb = info.getAllByte() / 1000;
			guiSnpCalling.getProgressBar().setValue((int) kb);
			if (info.getMessage() != null) {
				guiSnpCalling.getTxtInfo().setText(info.getMessage());
			}
		}
	}

	public void stop() {
		snpAnnotation.threadStop();
	}

	public void suspend() {
		snpAnnotation.threadSuspend();
	}

	public void resume() {
		snpAnnotation.threadResume();
	}

	@Override
	public void done(RunProcess runProcess) {
		if (guiSnpCalling != null) {
			guiSnpCalling.getProgressBar().setValue(guiSnpCalling.getProgressBar().getMaximum());
			guiSnpCalling.getTxtInfo().setText("Snp Annotation Complete");
			JOptionPane.showMessageDialog(null, "Snp Annotation Complete", "finish", JOptionPane.INFORMATION_MESSAGE);
			guiSnpCalling.getBtnAddPileupFile().setEnabled(true);
			guiSnpCalling.getBtnDeletePileup().setEnabled(true);
			guiSnpCalling.getBtnRun().setEnabled(true);
		}
	}

	@Override
	public void threadSuspended(RunProcess runProcess) {
		if (guiSnpCalling != null) {
			guiSnpCalling.getBtnRun().setEnabled(true);
		}
	}

	@Override
	public void threadResumed(RunProcess runProcess) {
		if (guiSnpCalling != null) {
			guiSnpCalling.getBtnRun().setEnabled(false);
		}
	}

	@Override
	public void threadStop(RunProcess runProcess) {
		if (guiSnpCalling != null) {
			guiSnpCalling.getBtnRun().setEnabled(true);
		}
	}
}
