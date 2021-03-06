package com.novelbio.nbcgui.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffType;
import com.novelbio.analysis.seq.resequencing.SnpLevel;
import com.novelbio.analysis.seq.sam.BcfTools;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.gui.JComboBoxData;
import com.novelbio.base.gui.JScrollPaneData;
import com.novelbio.database.model.species.Species;
import com.novelbio.nbcgui.controlseq.CtrlSnpAnnotation;
import com.novelbio.nbcgui.controlseq.CtrlSnpCalling;
import com.novelbio.nbcgui.controlseq.CtrlSnpGetInfo;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** snpCalling的界面 */
public class GuiSnpCallingFW extends JPanel implements GuiNeedOpenFile, GuiSnpCallingInt {
	private static final Logger logger = Logger.getLogger(GuiSnpCallingFW.class);
	
	private JTextField txtHetoSnpProp;
	JTextPane txtInformation;
	JButton btnAddPileupFile;
	JButton btnDeletePileupFile;
	JButton btnRun;
	JProgressBar progressBar;
	
	GUIFileOpen guiFileOpen = new GUIFileOpen();
	
	JScrollPaneData sclInputFile;
	JScrollPaneData sclSnpFile;
	
	private JButton btnAddSnpfile;
	private JButton btnDeleteSnpFile;
	private JTextField txtOutput;
	
	ButtonGroup buttonGroupSnpCallingFinding;
	JRadioButton rdbtnSnpcalling;
//	JRadioButton rdbtnGetSnpDetail;
	JRadioButton rdbtnSnpAnnotation;
	
	JSpinner spinColChrID;
	JSpinner spinColSnpStartSite;
	JSpinner spinColThisNr;
	JSpinner spinColRefNr;
	
	JButton btnOutput;
	
	CtrlSnpCalling ctrlSnpCalling = new CtrlSnpCalling(this);
	CtrlSnpGetInfo ctrlSnpGetInfo = new CtrlSnpGetInfo(this);
	CtrlSnpAnnotation ctrlSnpAnnotation = new CtrlSnpAnnotation(this);
	
	GffChrAbs gffChrAbs = new GffChrAbs();
	
	GuiLayeredPaneSpeciesVersionGff guiLayeredPaneSpeciesVersionGff;
	
	ButtonGroup buttonRadioSnpCalling = new ButtonGroup();
	JRadioButton rdbtnSamtoolsPileup;
	JRadioButton rdbtnGatkBamfile;
	JRadioButton rdbtnNbcmethodPileupfile;
	
	/**
	 * Create the panel.
	 */
	public GuiSnpCallingFW() {
		setLayout(null);
		
		sclInputFile = new JScrollPaneData();
		sclInputFile.setBounds(14, 38, 757, 128);
		add(sclInputFile);
		
		btnAddPileupFile = new JButton("AddFile");
		btnAddPileupFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> lsFileName = guiFileOpen.openLsFileName("PileupFile", "");
				for (String fileName : lsFileName) {
					if (rdbtnSnpcalling.isSelected()) {
						String outSnpFileName = FileOperate.changeFileSuffix(fileName, "_SnpInfo", "txt");
						sclInputFile.addItem(new String[]{fileName, outSnpFileName});
					}
					else {
						String sampleName = FileOperate.getFileNameSep(fileName)[0];
						sclInputFile.addItem(new String[]{fileName, sampleName});
					}
				}
			}
		});
		btnAddPileupFile.setBounds(783, 38, 118, 24);
		add(btnAddPileupFile);
		
		btnDeletePileupFile = new JButton("Delete");
		btnDeletePileupFile.setBounds(783, 151, 118, 24);
		btnDeletePileupFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sclInputFile.deleteSelRows();
			}
		});
		add(btnDeletePileupFile);
		
		JLabel lblHetolessinfo = new JLabel("Hetero SNP Reads Prop Level");
		lblHetolessinfo.setBounds(12, 178, 288, 14);
		add(lblHetolessinfo);
		
		txtHetoSnpProp = new JTextField();
		txtHetoSnpProp.setBounds(304, 174, 66, 24);
		add(txtHetoSnpProp);
		txtHetoSnpProp.setColumns(10);
		
		progressBar = new JProgressBar();
		progressBar.setBounds(14, 511, 767, 14);
		add(progressBar);
		
		btnRun = new JButton("Run");
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				progressBar.setValue(progressBar.getMinimum());
				if (rdbtnSnpcalling.isSelected()) {
//					if (rdbtnGatkBamfile.isSelected()) {
//						runSnpCallingGATK();
//					} else if (rdbtnSamtoolsPileup.isSelected()) {
//						runSnpCallingSamtools();
//					} else if (rdbtnNbcmethodPileupfile.isSelected()) {
//						runSnpCallingNBC();
//					}
					runSnpCallingNBC();
//				} else if (rdbtnGetSnpDetail.isSelected()) {
//					runSnpGetInfo();
				} else if (rdbtnSnpAnnotation.isSelected()) {
					runSnpAnnotation();
				}
			}
		});
		btnRun.setBounds(793, 501, 118, 24);
		add(btnRun);
		
		sclSnpFile = new JScrollPaneData();
		sclSnpFile.setBounds(14, 332, 754, 115);
		add(sclSnpFile);
		
		btnAddSnpfile = new JButton("AddSnpFile");
		btnAddSnpfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> lsFileName = guiFileOpen.openLsFileName("SnpFile", "");
				for (String fileName : lsFileName) {
					if (rdbtnSnpAnnotation.isSelected()) {
						String outSnpFileName = FileOperate.changeFileSuffix(fileName, "_Anno", "txt");
						sclSnpFile.addItem(new String[]{fileName, outSnpFileName});
					} else {
						sclSnpFile.addItem(new String[]{fileName});
					}
				}
			}
		});
		btnAddSnpfile.setBounds(795, 301, 118, 24);
		add(btnAddSnpfile);
		
		btnDeleteSnpFile = new JButton("Delete");
		btnDeleteSnpFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sclSnpFile.deleteSelRows();
			}
		});
		btnDeleteSnpFile.setBounds(795, 392, 118, 24);
		add(btnDeleteSnpFile);
		
		rdbtnSnpcalling = new JRadioButton("SNP/InDel Calling");
		rdbtnSnpcalling.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setSnpCalling();
			}
		});
		rdbtnSnpcalling.setBounds(14, 8, 191, 22);
		add(rdbtnSnpcalling);
		
//		rdbtnGetSnpDetail = new JRadioButton("get snp detail");
//		rdbtnGetSnpDetail.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				setSnpGetInfo();
//			}
//		});
//		rdbtnGetSnpDetail.setBounds(130, 8, 151, 22);
//		add(rdbtnGetSnpDetail);
		
		txtOutput = new JTextField();
		txtOutput.setBounds(19, 456, 565, 18);
		add(txtOutput);
		txtOutput.setColumns(10);
		
		btnOutput = new JButton("SaveTo");
		btnOutput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.saveFileName("snpDetail", "");
				txtOutput.setText(fileName);
			}
		});
		btnOutput.setBounds(609, 453, 118, 24);
		add(btnOutput);
		
		JLabel lblSnpsitefile = new JLabel("SNPSiteFile");
		lblSnpsitefile.setBounds(16, 306, 93, 14);
		add(lblSnpsitefile);
		
		txtInformation = new JTextPane();
		txtInformation.setText("Information");
		txtInformation.setBackground(UIManager.getColor("Button.background"));
		txtInformation.setEditable(false);
		txtInformation.setBounds(14, 475, 537, 34);
		add(txtInformation);
		
		JLabel lblColChrid = new JLabel("Col ChrID");
		lblColChrid.setBounds(115, 306, 69, 14);
		add(lblColChrid);
		
		spinColChrID = new JSpinner();
		spinColChrID.setBounds(185, 301, 43, 24);
		spinColChrID.setValue(1);
		add(spinColChrID);
		
		JLabel lblColSnpStart = new JLabel("Col SNP Start Site");
		lblColSnpStart.setBounds(246, 306, 136, 14);
		add(lblColSnpStart);
		
		spinColSnpStartSite = new JSpinner();
		spinColSnpStartSite.setBounds(389, 302, 50, 23);
		spinColSnpStartSite.setValue(2);
		add(spinColSnpStartSite);
		
		rdbtnSnpAnnotation = new JRadioButton("SNP/InDel annotation");
		rdbtnSnpAnnotation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setSnpAnnotation();
			}
		});
		rdbtnSnpAnnotation.setBounds(288, 8, 202, 22);
		add(rdbtnSnpAnnotation);
		
		JLabel lblColRefnr = new JLabel("Col RefNr");
		lblColRefnr.setBounds(457, 306, 69, 14);
		add(lblColRefnr);
		
		spinColRefNr = new JSpinner();
		spinColRefNr.setBounds(528, 304, 47, 21);
		spinColRefNr.setValue(3);
		add(spinColRefNr);
		
		JLabel lblColThisnr = new JLabel("Col ThisNr");
		lblColThisnr.setBounds(593, 306, 85, 14);
		add(lblColThisnr);
		
		spinColThisNr = new JSpinner();
		spinColThisNr.setBounds(680, 304, 51, 21);
		spinColThisNr.setValue(4);
		add(spinColThisNr);
		
		guiLayeredPaneSpeciesVersionGff = new GuiLayeredPaneSpeciesVersionGff();
//		guiLayeredPaneSpeciesVersionGff.addMouseListener(new MouseAdapter() {
//			@Override
//			public void mouseClicked(MouseEvent e) {
//				if (guiLayeredPaneSpeciesVersionGff.getSelectSpecies().getTaxID() == 0) {
//					txtRefOrGTF.setVisible(true);
//					btnOpenRefOrGFF.setVisible(true);
//					lblRefGFF.setVisible(true);
//				} else {
//					txtRefOrGTF.setVisible(false);
//					btnOpenRefOrGFF.setVisible(false);
//					lblRefGFF.setVisible(false);
//				}
//			}
//		});
//		guiLayeredPaneSpeciesVersionGff.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				if (guiLayeredPaneSpeciesVersionGff.getSelectSpecies().getTaxID() == 0) {
//					txtRefOrGTF.setVisible(true);
//					btnOpenRefOrGFF.setVisible(true);
//					lblRefGFF.setVisible(true);
//				} else {
//					txtRefOrGTF.setVisible(false);
//					btnOpenRefOrGFF.setVisible(false);
//					lblRefGFF.setVisible(false);
//				}
//			}
//		});
		guiLayeredPaneSpeciesVersionGff.setBounds(528, 170, 243, 122);
		add(guiLayeredPaneSpeciesVersionGff);

		initial();
	}
	
	private void initial() {
		buttonGroupSnpCallingFinding = new ButtonGroup();
//		buttonGroupSnpCallingFinding.add(rdbtnGetSnpDetail);
		buttonGroupSnpCallingFinding.add(rdbtnSnpcalling);
		buttonGroupSnpCallingFinding.add(rdbtnSnpAnnotation);
		rdbtnSnpcalling.setSelected(true);
		
		sclSnpFile.setTitle(new String[]{"Input SNP File"});
		
//		rdbtnGatkBamfile.setSelected(true);
		setSnpCalling();
		
		buttonRadioSnpCalling.add(rdbtnSamtoolsPileup);
		buttonRadioSnpCalling.add(rdbtnGatkBamfile);
		buttonRadioSnpCalling.add(rdbtnNbcmethodPileupfile);
		
		
	}
	/** 当为snpcalling时候的界面 */
	private void setSnpCalling() {
		txtHetoSnpProp.setEnabled(true);
//		rdbtnGatkBamfile.setEnabled(true);
//		rdbtnSamtoolsPileup.setEnabled(true);
//		rdbtnNbcmethodPileupfile.setEnabled(true);
		
		sclSnpFile.setEnabled(false);
		spinColChrID.setEnabled(false);
		spinColSnpStartSite.setEnabled(false);
		spinColRefNr.setEnabled(false);
		spinColThisNr.setEnabled(false);
		
		sclSnpFile.setEnabled(false);

		btnAddSnpfile.setEnabled(false);
		btnDeleteSnpFile.setEnabled(false);
		btnOutput.setVisible(false);
		txtOutput.setVisible(false);
		btnAddPileupFile.setEnabled(true);
		btnDeletePileupFile.setEnabled(true);
		
		//TODO
		guiLayeredPaneSpeciesVersionGff.setVisible(false);
		sclInputFile.setTitle(new String[]{"Input PileUp File","Output SNP File"});
	}
	
	/** 当为获得每个snp信息的时候的界面 */
	private void setSnpGetInfo() {
		sclInputFile.setTitle(new String[]{"Input PileUp File", "Sample Name"});
		txtHetoSnpProp.setEnabled(false);
//		rdbtnGatkBamfile.setEnabled(false);
//		rdbtnSamtoolsPileup.setEnabled(false);
//		rdbtnNbcmethodPileupfile.setEnabled(false);
		
		sclSnpFile.setEnabled(true);
		sclSnpFile.setTitle(new String[]{"Input Snp File"});
		spinColChrID.setEnabled(true);
		spinColSnpStartSite.setEnabled(true);
		spinColRefNr.setEnabled(false);
		spinColThisNr.setEnabled(false);
		
		sclSnpFile.setEnabled(true);
		btnAddSnpfile.setEnabled(true);
		btnDeleteSnpFile.setEnabled(true);
		btnOutput.setVisible(true);
		txtOutput.setVisible(true);
		btnAddPileupFile.setEnabled(true);
		btnDeletePileupFile.setEnabled(true);
		
		guiLayeredPaneSpeciesVersionGff.setVisible(false);
	}
	/** 当为获得每个snp信息的时候的界面 */
	private void setSnpAnnotation() {
		sclInputFile.setEnabled(false);
		btnAddPileupFile.setEnabled(false);
		btnDeletePileupFile.setEnabled(false);
//		rdbtnGatkBamfile.setEnabled(false);
//		rdbtnSamtoolsPileup.setEnabled(false);
//		rdbtnNbcmethodPileupfile.setEnabled(false);
		txtHetoSnpProp.setEnabled(false);
		
		txtOutput.setVisible(false);
		btnOutput.setVisible(false);
		
		sclSnpFile.setEnabled(true);
		sclSnpFile.setTitle(new String[]{"Input Snp File", "Output File"});
		spinColChrID.setEnabled(true);
		spinColSnpStartSite.setEnabled(true);
		spinColRefNr.setEnabled(true);
		spinColThisNr.setEnabled(true);

		sclSnpFile.setEnabled(true);
		btnAddSnpfile.setEnabled(true);
		btnDeleteSnpFile.setEnabled(true);
		btnOutput.setVisible(false);
		txtOutput.setVisible(false);
		
		guiLayeredPaneSpeciesVersionGff.setVisible(true);
//		lblRefGFF.setText("GFF");
	}
	
	
	private void runSnpCallingNBC() {
//		setGffChrAbs();
//		ctrlSnpCalling.setGffChrAbs(gffChrAbs);

		ctrlSnpCalling.setSnpFilterLevel(SnpLevel.HeteroMore);
		ArrayList<String[]> lsFile = sclInputFile.getLsDataInfo();
		for (String[] strings : lsFile) {
			ctrlSnpCalling.addSnpFromPileUpFile(strings[0], strings[1]);
		}
		ctrlSnpCalling.setSnp_Hete_Contain_SnpProp_Min(0);
		
		try {
			double hetoMoreSnpProp = Double.parseDouble(txtHetoSnpProp.getText());
			ctrlSnpCalling.setSnp_HetoMore_Contain_SnpProp_Min(hetoMoreSnpProp);
		} catch (Exception e2) { }
		
		ctrlSnpCalling.runSnpCalling();
	}
	
	private void runSnpCallingGATK() {
		ArrayList<String[]> lsFile = sclInputFile.getLsDataInfo();
		for (String[] strings : lsFile) {
			SamFile samFile = new SamFile(strings[0]);
			String refSeq = "";
			if (guiLayeredPaneSpeciesVersionGff.getSelectSpecies().getTaxID() == 0) {
//				refSeq = txtRefOrGTF.getText();
			} else {
				refSeq = guiLayeredPaneSpeciesVersionGff.getSelectSpecies().getChromSeq();
			}
			
			samFile.setReferenceFileName(refSeq);
			//TODO 整合GATK
		}
	}
	
	private void runSnpCallingSamtools() {
		ArrayList<String[]> lsFile = sclInputFile.getLsDataInfo();
		for (String[] strings : lsFile) {
			BcfTools bcfTools = new BcfTools(strings[0], strings[1]);
			if (!bcfTools.snpCalling()) {
				logger.error("samtools call snp error:" + strings[1]);
			}
		}
	}
	
	private void runSnpGetInfo() {
//		setGffChrAbs();
		
//		ctrlSnpGetInfo.setGffChrAbs(gffChrAbs);
		ArrayList<String[]> lsPileupFile = sclInputFile.getLsDataInfo();
		for (String[] strings : lsPileupFile) {
			ctrlSnpGetInfo.addPileupFile(strings[1], strings[0]);
		}
		ArrayList<String> lsFiles = new ArrayList<String>();
		for (String[] strings : sclSnpFile.getLsDataInfo()) {
			lsFiles.add(strings[0]);
		}
		int colChrID = (Integer)spinColChrID.getValue();
		int colSiteStart = (Integer) spinColSnpStartSite.getValue();
		ctrlSnpGetInfo.setLsReadFile(lsFiles, colChrID, colSiteStart);
		ctrlSnpGetInfo.setOutfile(txtOutput.getText());
		ctrlSnpGetInfo.runSnpGetInfo();
	}
	private void runSnpAnnotation() {
		setGffChrAbs();
		
		ctrlSnpAnnotation.setGffChrAbs(gffChrAbs);
		ArrayList<String[]> lsSnpFile = sclSnpFile.getLsDataInfo();
		for (String[] strings : lsSnpFile) {
			ctrlSnpAnnotation.addSnpFile(strings[0], strings[1]);
		}
		int colChrID = (Integer)spinColChrID.getValue();
		int colRefStartSite = (Integer) spinColSnpStartSite.getValue();
		int colRefNr = (Integer) spinColRefNr.getValue();
		int colThisNr = (Integer) spinColThisNr.getValue();
		ctrlSnpAnnotation.setCol(colChrID, colRefStartSite, colRefNr, colThisNr);
		ctrlSnpAnnotation.runAnnotation();
	}
	private void setGffChrAbs() {
		Species species = guiLayeredPaneSpeciesVersionGff.getSelectSpecies();
//		if (species.getTaxID() == 0 && !txtRefOrGTF.getText().equals("")) {
//			//TODO 待补充GTF的类型
//			gffChrAbs.setGffHash(new GffHashGene(GffType.NCBI, txtRefOrGTF.getText()));
//			return;
//		}
		gffChrAbs.setSpecies(species);
	}
	
	public void setGuiFileOpen(GUIFileOpen guiFileOpen) {
		this.guiFileOpen = guiFileOpen;
	}
	
	public JProgressBar getProgressBar() {
		return progressBar;
	}
	/** 读取信息 */
	public JTextPane getTxtInfo() {
		return txtInformation;
	}
	public JButton getBtnAddPileupFile() {
		return btnAddPileupFile;
	}
	public JButton getBtnDeletePileup() {
		return btnDeletePileupFile;
	}
	public JButton getBtnAddSnpFile() {
		return btnAddSnpfile;
	}
	public JButton getBtnDeleteSnp() {
		return btnDeleteSnpFile;
	}
	public JButton getBtnRun() {
		return btnRun;
	}
}
