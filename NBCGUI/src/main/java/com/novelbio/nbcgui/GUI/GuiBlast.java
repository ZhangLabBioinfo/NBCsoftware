package com.novelbio.nbcgui.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.novelbio.analysis.annotation.blast.BlastNBC;
import com.novelbio.analysis.annotation.blast.BlastType;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.fasta.StrandType;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.gui.JComboBoxData;
import com.novelbio.base.gui.JScrollPaneData;
import com.novelbio.base.gui.JTextFieldData;
import com.novelbio.database.domain.geneanno.BlastFileInfo;
import com.novelbio.database.model.species.Species;
import com.novelbio.database.model.species.Species.EnumSpeciesType;
import com.novelbio.database.updatedb.database.BlastUp2DB;
import com.novelbio.nbcgui.controlquery.CtrlBlast;

public class GuiBlast extends JPanel implements GuiNeedOpenFile {
	private JTextField textQueryFasta;
	private JTextField textSubjectFasta;
	private JTextField textEvalue;
	private JTextFieldData textResultNum;
	private JTextField textResultFile;
	
	private CtrlBlast ctrlBlast = new CtrlBlast();
	
	GUIFileOpen fileOpen = new GUIFileOpen();
	JCheckBox chbRefStyle = null;

	JComboBoxData<BlastType> combBlastType = null;
	JComboBoxData<Integer> combResultType = null;
	JComboBoxData<StrandType> combStrand = null;
	
	JCheckBox chckbxSavetodb;
	JScrollPaneData sclPaneBlastFile;
	JCheckBox chckbxQueryseqshortthan;
	
	int queryType = SeqFasta.SEQ_UNKNOWN;
	int subjectType = SeqFasta.SEQ_UNKNOWN;
	private JTextField txtThreadNum;
		
	private void setCombBlastType() {
		combBlastType.setMapItem(BlastType.getMapBlastType(queryType, subjectType));
	}
	
	
	/**
	 * Create the panel.
	 */
	public GuiBlast() {
		setLayout(null);
		
		textQueryFasta = new JTextField();
		textQueryFasta.setBounds(10, 50, 249, 21);
		add(textQueryFasta);
		textQueryFasta.setColumns(10);
		
		JButton btnQueryFasta = new JButton("QueryFasta");
		btnQueryFasta.setBounds(279, 48, 130, 24);
		btnQueryFasta.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String path = fileOpen.openFileName("txt", "");
				textQueryFasta.setText(path);
				if (FileOperate.isFileExistAndNotDir(path)) {
					queryType = SeqHash.getSeqType(path);
				}
				setCombBlastType();
			}
		});
		add(btnQueryFasta);
		
		textSubjectFasta = new JTextField();
		textSubjectFasta.setBounds(10, 97, 249, 21);
		add(textSubjectFasta);
		textSubjectFasta.setColumns(10);
		
		JButton btnSubjectDB = new JButton("SubjectFasta");
		btnSubjectDB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String path = fileOpen.openFileName("txt", "");
				textSubjectFasta.setText(path);
				if (FileOperate.isFileExistAndNotDir(path)) {
					subjectType = SeqHash.getSeqType(path);
				}
				setCombBlastType();
			}
		});
		btnSubjectDB.setBounds(279, 95, 130, 24);
		add(btnSubjectDB);
		
		combBlastType = new JComboBoxData<BlastType>();
		combBlastType.setBounds(10, 175, 209, 23);
		add(combBlastType);
		
		combResultType = new JComboBoxData<Integer>();
		combResultType.setMapItem(BlastNBC.getHashResultType());
		combResultType.setBounds(10, 440, 249, 23);
		add(combResultType);
		
		combStrand = new JComboBoxData<>();
		combStrand.setMapItem(BlastNBC.getMapStrandType2Value());
		combStrand.setBounds(144, 296, 146, 24);
		add(combStrand);
		
		JLabel lblStrand = new JLabel("Strand");
		lblStrand.setBounds(144, 279, 98, 15);
		add(lblStrand);
		JLabel lblBlastType = new JLabel("BlastType");
		lblBlastType.setBounds(10, 152, 91, 14);
		add(lblBlastType);
		
		textEvalue = new JTextField();
		textEvalue.setText("0.01");
		textEvalue.setBounds(10, 240, 114, 18);
		add(textEvalue);
		textEvalue.setColumns(10);
		
		JLabel lblEvalue = new JLabel("E-Value");
		lblEvalue.setBounds(10, 216, 54, 14);
		add(lblEvalue);
		
		textResultNum = new JTextFieldData();
		textResultNum.setNumOnly();
		textResultNum.setText("2");
		textResultNum.setBounds(10, 299, 114, 18);
		add(textResultNum);
		textResultNum.setColumns(10);
		
		JLabel lblResultnum = new JLabel("ResultNum");
		lblResultnum.setBounds(10, 279, 77, 14);
		add(lblResultnum);
		
		textResultFile = new JTextField();
		textResultFile.setBounds(10, 367, 249, 21);
		add(textResultFile);
		textResultFile.setColumns(10);
		
		JButton btnResultfile = new JButton("ResultFile");
		btnResultfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textResultFile.setText(fileOpen.openFileName("OutFile", ""));
			}
		});
		btnResultfile.setBounds(279, 365, 105, 24);
		add(btnResultfile);
		
		JButton btnRunblast = new JButton("RunBlast");
		btnRunblast.setBounds(279, 439, 98, 24);
		btnRunblast.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ctrlBlast.setBlastType(combBlastType.getSelectedValue());
				try {
					ctrlBlast.setCpuNum(Integer.parseInt(txtThreadNum.getText()));
				} catch (Exception e2) { }
				ctrlBlast.setDatabaseSeq(textSubjectFasta.getText());
				ctrlBlast.setQueryFastaFile(textQueryFasta.getText());
				ctrlBlast.setEvalue(Double.parseDouble(textEvalue.getText()));
				ctrlBlast.setResultAlignNum(Integer.parseInt(textResultNum.getText()));
				ctrlBlast.setResultSeqNum(Integer.parseInt(textResultNum.getText()));
				ctrlBlast.setResultFile(textResultFile.getText());
				ctrlBlast.setShortQuerySeq(chckbxQueryseqshortthan.isSelected());
				ctrlBlast.setResultType((Integer)combResultType.getSelectedValue());
				ctrlBlast.setStrandType(combStrand.getSelectedValue());
				ctrlBlast.blast();
			}
		});
		add(btnRunblast);
		
		JButton btnNewButton = new JButton("OpenBlastFile");
		btnNewButton.setBounds(446, 365, 146, 24);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<String> lsPath = fileOpen.openLsFileName("txt", "");
				List<String[]> lsInput = new ArrayList<String[]>();
				for (String path : lsPath) {
					lsInput.add(new String[]{path, "", ""});
				}
				sclPaneBlastFile.addItemLs(lsInput);
			}
		});
		add(btnNewButton);
		
		JButton btnUpdataBlast = new JButton("UpdataBlast");
		btnUpdataBlast.setBounds(783, 450, 123, 24);
		btnUpdataBlast.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addBlastInfo();
			}
		});
		add(btnUpdataBlast);
		
		JLabel lblResulttype = new JLabel("ResultType");
		lblResulttype.setBounds(10, 414, 91, 14);
		add(lblResulttype);
		
		chckbxSavetodb = new JCheckBox("SaveToDB");
		chckbxSavetodb.setBounds(775, 410, 131, 22);
		add(chckbxSavetodb);
		
		sclPaneBlastFile = new JScrollPaneData();
		sclPaneBlastFile.setBounds(446, 43, 460, 310);
		add(sclPaneBlastFile);
		
		JButton btnDelfile = new JButton("DelFile");
		btnDelfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sclPaneBlastFile.deleteSelRows();
			}
		});
		btnDelfile.setBounds(760, 365, 146, 24);
		add(btnDelfile);
		
		txtThreadNum = new JTextField();
		txtThreadNum.setText("4");
		txtThreadNum.setBounds(144, 240, 64, 18);
		add(txtThreadNum);
		txtThreadNum.setColumns(10);
		
		JLabel lblThreadnum = new JLabel("ThreadNum");
		lblThreadnum.setBounds(146, 216, 98, 14);
		add(lblThreadnum);
		
		chckbxQueryseqshortthan = new JCheckBox("Query Seq Short Than 60Bp");
		chckbxQueryseqshortthan.setBounds(227, 173, 209, 26);
		add(chckbxQueryseqshortthan);
		

		
		initial();
	}
	
	private void initial() {
		JComboBoxData<Species> cmbSpeciesQ  = new JComboBoxData<Species>();
		cmbSpeciesQ.setMapItem(Species.getSpeciesName2Species(EnumSpeciesType.All));
		cmbSpeciesQ.setEditable(true);
		JComboBoxData<Species> cmbSpeciesS  = new JComboBoxData<Species>();
		cmbSpeciesS.setMapItem(Species.getSpeciesName2Species(EnumSpeciesType.All));
		cmbSpeciesS.setEditable(false);
		
		sclPaneBlastFile.setTitle(new String[]{"BlastFile","QueryTaxID", "SubTaxID"});
		sclPaneBlastFile.setItem(1, cmbSpeciesQ);
		sclPaneBlastFile.setItem(2, cmbSpeciesS);
	}
	
	private void addBlastInfo() {
		Map<String, Species> mapComName2Species = Species.getSpeciesName2Species(EnumSpeciesType.All);
		for (String[] content : sclPaneBlastFile.getLsDataInfo()) {
			BlastFileInfo blastFileInfo = new BlastFileInfo();
			blastFileInfo.setTmp(chckbxSavetodb.isSelected());
			Species speciesQ = mapComName2Species.get(content[1]);
			Species speciesS = mapComName2Species.get(content[2]);
			
			blastFileInfo.setQueryTaxID(speciesQ.getTaxID());
			blastFileInfo.setSubjectTaxID(speciesS.getTaxID());
			blastFileInfo.setBlastType(BlastType.blastp);
			blastFileInfo.setFileName(content[0]);
			
			try {
				blastFileInfo.importAndSave();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null,content[0].trim() + "cannot update\n" + e.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
			}
			BlastUp2DB blast = new BlastUp2DB();
		}
	}
	
	public void setGuiFileOpen(GUIFileOpen guiFileOpen) {
		this.fileOpen = guiFileOpen;
	}
}
