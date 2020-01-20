import java.awt.EventQueue;


import javax.swing.JFrame;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.awt.event.ActionEvent;
import java.awt.Color;
import javax.swing.JTextPane;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTable;

public class OsMowSisApp {

	private JFrame frame;
	private JTextField total_turn;
	private JTextField current_turn;
	private JTextField total_sq;
	private JTextField total_cut;
    JFileChooser fileChooser = new JFileChooser();
    StringBuilder sb = new StringBuilder();
    private JTable table;
    private JTextField next_object;
    private JTable table_1;
    private JTable status_table;
    SimulationSystem sys;
    ScenarioFileParser fileParser;
    private JTextField remaining_grass;
    DefaultTableModel mower_table;
    DefaultTableModel graphic_table;
    private int lawn_width;
    private int lawn_height;
    JTable graph_table;
    private URL urlEmpty = getClass().getResource("images/empty.png");
	private ImageIcon emptyIcon = scaleImage(new ImageIcon(urlEmpty), 100, 50);
	private URL urlGrass = getClass().getResource("images/grass.png");
	private ImageIcon grassIcon = scaleImage(new ImageIcon(urlGrass), 100, 50);
	private URL urlGopherEmpty = getClass().getResource("images/gopher_empty.png");
	private ImageIcon gopherEmptyIcon = scaleImage(new ImageIcon(urlGopherEmpty), 100, 50);
	private URL urlGopherGrass = getClass().getResource("images/gopher_grass.png");
	private ImageIcon gopherGrassIcon = scaleImage(new ImageIcon(urlGopherGrass), 100, 50);
	private URL urlRecharge = getClass().getResource("images/recharge.png");
	private  ImageIcon rechargeIcon = scaleImage(new ImageIcon(urlRecharge), 100, 50);
	private URL urlCrashMower = getClass().getResource("images/crash_mower.png");
	private ImageIcon crashMowerIcon = scaleImage(new ImageIcon(urlCrashMower), 100, 50);
	private URL urlMower = getClass().getResource("images/mower.png");
	private ImageIcon mowerIcon = scaleImage(new ImageIcon(urlMower), 100, 50);
    
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
    	System.out.println(System.getProperty("user.dir"));
    	
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					OsMowSisApp window = new OsMowSisApp();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

    public void importFile() throws Exception {
    	if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
    		java.io.File file = fileChooser.getSelectedFile();
    		OsMowSisApp m = new OsMowSisApp();
//    		sys = m.loadFile(file.getName(), true);
    		sys = m.loadFile(file.getAbsolutePath(), true);

            //Place mowers on their initial locations on the lawn in the simulator
        	sys.placeInitialMowers();
        	
//            //TODO: testing in FastForward mode
//            s.activateFastForward();

            //Begin simulation
//        	sys.run();
    	}
    	else {
//    		sb.append("No file was selected");
    	}
    }

    public ImageIcon scaleImage(ImageIcon icon, int w, int h)
    {
        int nw = icon.getIconWidth();
        int nh = icon.getIconHeight();

        if(icon.getIconWidth() > w)
        {
          nw = w;
          nh = (nw * icon.getIconHeight()) / icon.getIconWidth();
        }

        if(nh > h)
        {
          nh = h;
          nw = (icon.getIconWidth() * nh) / icon.getIconHeight();
        }

        return new ImageIcon(icon.getImage().getScaledInstance(nw, nh, Image.SCALE_DEFAULT));
    }
    
    public SimulationSystem loadFile(String scenarioFileName, boolean isVerbose){

        fileParser = new ScenarioFileParser(scenarioFileName, isVerbose);
        System.out.println("Charles debug :: " + scenarioFileName);
//        maxTurn = fileParser.maxTurns;
//        total_turn.setText(Integer.toString(maxTurn));
    	
        //Create instance of Lawn class
        Lawn theLawn = fileParser.loadAndCreateLawn();

        //Create the instances of mowers
        ArrayList<Mower> mowers = fileParser.loadAndCreateMowers();

        //Create the instances of gophers
        ArrayList<Gopher> gophers = fileParser.loadAndCreateGophers();

        //Create the mower state tracker
        ArrayList<String> mowerStateTracker = fileParser.loadAndCreateMowerStateTracker();

        //Create simulator system service that runs, manages, and monitors the simulation.
        //The Mower class cannot track the state of the outside world so need this simulator system service to track state and monitor Mower behavior.
        SimulationSystem sys = new SimulationSystem(theLawn, mowers, mowerStateTracker, gophers, isVerbose);
        
        return sys;
    }

	/**
	 * Create the application.
	 */
	public OsMowSisApp() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		Object [][] image_rows = {
				{grassIcon}
		};
		frame = new JFrame();
		frame.setBounds(100, 100, 1480, 877);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setToolTipText("");
		panel.setBackground(Color.WHITE);
		panel.setBounds(1204, 39, 250, 201);
		frame.getContentPane().add(panel);
		panel.setLayout(null);
		
		JLabel lblTotalTurns = new JLabel("Total Turns");
		lblTotalTurns.setBounds(10, 14, 93, 14);
		
		panel.add(lblTotalTurns);
		
		JLabel lblCurrentTurns = new JLabel("Current Turns");
		lblCurrentTurns.setBounds(10, 48, 93, 14);
		panel.add(lblCurrentTurns);
		
		JLabel lblTotalSquares = new JLabel("Total Squares");
		lblTotalSquares.setBounds(10, 79, 93, 14);
		panel.add(lblTotalSquares);
		
		JLabel lblTotalCut = new JLabel("Total Grasses Cut");
		lblTotalCut.setBounds(10, 110, 105, 14);
		panel.add(lblTotalCut);
		
		JLabel lblGrassesRemaining = new JLabel("Grasses Remaining");
		lblGrassesRemaining.setBounds(10, 141, 105, 14);
		panel.add(lblGrassesRemaining);
		
		JLabel lblNextObject = new JLabel("Next Object");
		lblNextObject.setBounds(10, 172, 105, 14);
		panel.add(lblNextObject);
		
		total_turn = new JTextField();
		total_turn.setEditable(false);
		total_turn.setBounds(136, 11, 86, 20);
		panel.add(total_turn);
		total_turn.setColumns(10);
		
		current_turn = new JTextField();
		current_turn.setEditable(false);
		current_turn.setColumns(10);
		current_turn.setBounds(136, 45, 86, 20);
		panel.add(current_turn);
		
		total_sq = new JTextField();
		total_sq.setEditable(false);
		total_sq.setColumns(10);
		total_sq.setBounds(136, 76, 86, 20);
		panel.add(total_sq);
		
		total_cut = new JTextField();
		total_cut.setEditable(false);
		total_cut.setColumns(10);
		total_cut.setBounds(136, 107, 86, 20);
		panel.add(total_cut);
		
		next_object = new JTextField();
		next_object.setEditable(false);
		next_object.setColumns(10);
		next_object.setBounds(136, 169, 86, 20);
		panel.add(next_object);
		
		remaining_grass = new JTextField();
		remaining_grass.setEditable(false);
		remaining_grass.setColumns(10);
		remaining_grass.setBounds(136, 138, 86, 20);
		panel.add(remaining_grass);

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(null);
		panel_2.setBounds(348, 793, 553, 38);
		frame.getContentPane().add(panel_2);
		
		JButton loadFile = new JButton("Load File");
		loadFile.setBounds(82, 5, 100, 29);
		loadFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					importFile();
				} catch(Exception e) {
					e.printStackTrace();
				}
				
				// Summary Table
				total_turn.setText(Integer.toString(ScenarioFileParser.maxTurns));
				current_turn.setText(Integer.toString(sys.getLawn().getCurrentTurnNumber()));
				total_sq.setText(Integer.toString(sys.getLawn().getTotalSquare()));
				total_cut.setText(Integer.toString(sys.getLawn().getCurrentNumberCut()));
				remaining_grass.setText(Integer.toString(sys.getLawn().getGrassesRemaining()));
				next_object.setText(sys.getNextObjectPolled());
				
				ArrayList<Mower> mowers = sys.getMowers();
				ArrayList<Gopher> gophers = sys.getGophers();
				Direction[] directions = sys.getMowerStateDirection();
				boolean[] status = sys.getMowerStateRunning();
				int[] energy = sys.getMowerStateEnergy();
				lawn_width = sys.getLawn().getWidth();
				lawn_height = sys.getLawn().getHeight();
				int[] mowerX = sys.getMowerStateX();
				int[] mowerY = sys.getMowerStateY();
				
				// Mower Status Table
				mower_table.setRowCount(0);
				Object[] row = new Object[5];
				
				String[] string_status = new String[status.length];
				for(int i = 0; i < status.length; i++) {
					if (status[i] == true) {
						string_status[i] = "On";
					} else {
						string_status[i] = "Off or Crash";
					}
				}
				
				for(int i = 0; i < directions.length; i++) {
//					mower_table.setValueAt("m" + i, i, 0);
//					mower_table.setValueAt(status[i], i, 1);
//					mower_table.setValueAt(directions[i], i, 2);
//					mower_table.setValueAt(energy[i], i, 3);
					
					row[0] = "m" + i;
					row[1] = (mowerX[i] + 1) + ", " + (mowerY[i] + 1);
					row[2] = string_status[i];
					row[3] = directions[i];
					row[4] = energy[i];
					mower_table.addRow(row);
				}
				
				Object[] graph_column = new Object[lawn_width];;
				for(int i = 0; i < lawn_height; i++) {
					graph_column[i] = "";
				}
				

//				for(int k = 0; k < mowerX.length; k++) {
//					Point mowerWithID = new Point(mowerX[k], mowerY[k]);
//				}
				
				graphic_table = new DefaultTableModel() {
					public Class getColumnClass(int column)
		            {
		                return getValueAt(0, column).getClass();
		            }
				};
				graphic_table.setColumnIdentifiers(graph_column);
				graph_table.setModel(graphic_table);
				
				Object[][] graphic_row = new Object[lawn_height][lawn_width];
				graphic_table.setRowCount(0);
				for(int i = lawn_height - 1; i >= 0; i--) {
					for(int j = 0; j <= lawn_width; j++) {
					    Square theSq = sys.getLawn().getSquareAt(j, i);
					    // Replace with image
					    switch (theSq.getSquareKind()) {
					    case "empty":
					        graphic_row[i][j] = emptyIcon;
					        break;
					    case "recharge":
					        graphic_row[i][j] = rechargeIcon;
					        break;
					    case "grass":
					        graphic_row[i][j] = grassIcon;
					        break;
					    case "gopher_empty":
					        graphic_row[i][j] = gopherEmptyIcon;
					        break;
					    case "gopher_grass":
					        graphic_row[i][j] = gopherGrassIcon;
					        break;
					    default:
					        break;
						}
					    
						for(int k = 0; k < mowerX.length; k++) {
							if (j == mowerX[k] && i == mowerY[k]) {
								if (j == mowerX[k] && i == mowerY[k]) {
									if (status[k] == false) {
									    // Replace with image
										graphic_row[i][j] = crashMowerIcon;
									} else {
								    // Mower stop or crash; replace with image
								        graphic_row[i][j] = mowerIcon;
									}
								}
							}
						}
					}
                    graphic_table.addRow(graphic_row[i]);
				}
			}
		});
		panel_2.setLayout(null);
		panel_2.add(loadFile);

		JButton next = new JButton("Next");
		next.setBounds(187, 5, 75, 29);
		next.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
		        	sys.pressIncrement();
					total_turn.setText(Integer.toString(ScenarioFileParser.maxTurns));
					current_turn.setText(Integer.toString(sys.getLawn().getCurrentTurnNumber()));
					total_sq.setText(Integer.toString(sys.getLawn().getTotalSquare()));
					total_cut.setText(Integer.toString(sys.getLawn().getCurrentNumberCut()));
					remaining_grass.setText(Integer.toString(sys.getLawn().getGrassesRemaining()));
					next_object.setText(sys.getNextObjectPolled());
					
					ArrayList<Mower> mowers = sys.getMowers();
					ArrayList<Gopher> gophers = sys.getGophers();
					Direction[] directions = sys.getMowerStateDirection();
					boolean[] status = sys.getMowerStateRunning();
					int[] energy = sys.getMowerStateEnergy();
					int[] mowerX = sys.getMowerStateX();
					int[] mowerY = sys.getMowerStateY();
					
					Object[] row = new Object[5];
					String[] string_status = new String[status.length];
					for(int i = 0; i < status.length; i++) {
						if (status[i] == true) {
							string_status[i] = "On";
						} else {
							string_status[i] = "Off or Crash";
						}
					}
					
					for(int i = 0; i < directions.length; i++) {
						mower_table.setValueAt("m" + i, i, 0);
						mower_table.setValueAt((mowerX[i] + 1) + ", " + (mowerY[i] + 1), i, 1);
						mower_table.setValueAt(string_status[i], i, 2);
						mower_table.setValueAt(directions[i], i, 3);
						mower_table.setValueAt(energy[i], i, 4);
					}
					
					Object[] graph_column = new Object[lawn_width];;
					for(int i = 0; i < lawn_height; i++) {
						graph_column[i] = "";
					}
					
					graphic_table.setColumnIdentifiers(graph_column);
					graph_table.setModel(graphic_table);
					
					Object[][] graphic_row = new Object[lawn_height][lawn_width];
					graphic_table.setRowCount(0);
					for(int i = lawn_height - 1; i >= 0; i--) {
						for(int j = 0; j <= lawn_width; j++) {
						    Square theSq = sys.getLawn().getSquareAt(j, i);
						    // Replace with image
						    switch (theSq.getSquareKind()) {
						    case "empty":
						        graphic_row[i][j] = emptyIcon;
						        break;
						    case "recharge":
						        graphic_row[i][j] = rechargeIcon;
						        break;
						    case "grass":
						        graphic_row[i][j] = grassIcon;
						        break;
						    case "gopher_empty":
						        graphic_row[i][j] = gopherEmptyIcon;
						        break;
						    case "gopher_grass":
						        graphic_row[i][j] = gopherGrassIcon;
						        break;
						    default:
						        break;
							}
						    
							for(int k = 0; k < mowerX.length; k++) {
								if (j == mowerX[k] && i == mowerY[k]) {
									if (j == mowerX[k] && i == mowerY[k]) {
										if (status[k] == false) {
										    // Replace with image
											graphic_row[i][j] = crashMowerIcon;
										} else {
									    // Mower stop or crash; replace with image
									        graphic_row[i][j] = mowerIcon;
										}
									}
								}
							}
						}
	                    graphic_table.addRow(graphic_row[i]);
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		panel_2.add(next);

		JButton fast_forward = new JButton("Fast Forward");
		fast_forward.setBounds(267, 5, 124, 29);
		fast_forward.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
		        	sys.activateFastForward();
					total_turn.setText(Integer.toString(ScenarioFileParser.maxTurns));
					current_turn.setText(Integer.toString(sys.getLawn().getCurrentTurnNumber()));
					total_sq.setText(Integer.toString(sys.getLawn().getTotalSquare()));
					total_cut.setText(Integer.toString(sys.getLawn().getCurrentNumberCut()));
					remaining_grass.setText(Integer.toString(sys.getLawn().getGrassesRemaining()));
					next_object.setText(sys.getNextObjectPolled());
					
					ArrayList<Mower> mowers = sys.getMowers();
					ArrayList<Gopher> gophers = sys.getGophers();
					Direction[] directions = sys.getMowerStateDirection();
					boolean[] status = sys.getMowerStateRunning();
					int[] energy = sys.getMowerStateEnergy();
					int[] mowerX = sys.getMowerStateX();
					int[] mowerY = sys.getMowerStateY();
					
					Object[] row = new Object[5];
					String[] string_status = new String[status.length];
					for(int i = 0; i < status.length; i++) {
						if (status[i] == true) {
							string_status[i] = "On";
						} else {
							string_status[i] = "Off or Crash";
						}
					}
					
					for(int i = 0; i < directions.length; i++) {
						mower_table.setValueAt("m" + i, i, 0);
						mower_table.setValueAt((mowerX[i] + 1) + ", " + (mowerY[i] + 1), i, 1);
						mower_table.setValueAt(string_status[i], i, 2);
						mower_table.setValueAt(directions[i], i, 3);
						mower_table.setValueAt(energy[i], i, 4);
					}
					
					Object[] graph_column = new Object[lawn_width];;
					for(int i = 0; i < lawn_height; i++) {
						graph_column[i] = "";
					}
					
					graphic_table.setColumnIdentifiers(graph_column);
					graph_table.setModel(graphic_table);
					
					Object[][] graphic_row = new Object[lawn_height][lawn_width];
					graphic_table.setRowCount(0);
					for(int i = lawn_height - 1; i >= 0; i--) {
						for(int j = 0; j <= lawn_width; j++) {
						    Square theSq = sys.getLawn().getSquareAt(j, i);
						    
						    // Replace with image
						    switch (theSq.getSquareKind()) {
						    case "empty":
						        graphic_row[i][j] = emptyIcon;
						        break;
						    case "recharge":
						        graphic_row[i][j] = rechargeIcon;
						        break;
						    case "grass":
						        graphic_row[i][j] = grassIcon;
						        break;
						    case "gopher_empty":
						        graphic_row[i][j] = gopherEmptyIcon;
						        break;
						    case "gopher_grass":
						        graphic_row[i][j] = gopherGrassIcon;
						        break;
						    default:
						        break;
							}
						    
							for(int k = 0; k < mowerX.length; k++) {
								if (j == mowerX[k] && i == mowerY[k]) {
									if (j == mowerX[k] && i == mowerY[k]) {
										if (status[k] == false) {
										    // Replace with image
											graphic_row[i][j] = crashMowerIcon;
										} else {
									    // Mower stop or crash; replace with image
									        graphic_row[i][j] = mowerIcon;
										}
									}
								}
							}
						}
	                    graphic_table.addRow(graphic_row[i]);
					}
					
	                JOptionPane.showMessageDialog(frame, "The simulation has ended. Please check summary section for detail result.");
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		panel_2.add(fast_forward);

		JButton stop = new JButton("Stop");
		stop.setBounds(396, 5, 75, 29);
		stop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
	                JOptionPane.showMessageDialog(frame, "The simulation has ended. Please check summary section for detail result.");
		        	sys.stop();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		panel_2.add(stop);
		
		JScrollPane graph_panel = new JScrollPane();
		graph_panel.setBackground(Color.WHITE);
		graph_panel.setBounds(6, 6, 1188, 776);
		frame.getContentPane().add(graph_panel);
		
		graph_table = new JTable();
		

		graph_table.setTableHeader(null);
		graph_panel.setViewportView(graph_table);
		graph_table.setRowHeight(70);
		panel_2.setLayout(null);
		
		JLabel summaryTitle = new JLabel("Summary");
		summaryTitle.setFont(new Font("Tahoma", Font.BOLD, 14));
		summaryTitle.setBounds(1204, 11, 105, 16);
		frame.getContentPane().add(summaryTitle);
		
		JLabel outputTitle = new JLabel("Mower Status");
		outputTitle.setFont(new Font("Tahoma", Font.BOLD, 14));
		outputTitle.setBounds(1204, 251, 105, 14);
		frame.getContentPane().add(outputTitle);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(1204, 276, 250, 505);
		frame.getContentPane().add(scrollPane);
		
		JTable mo_table = new JTable();
		
		Object[] statusColumnNames = {"Mower", "location", "Status", "Direction", "energy"};
		mower_table = new DefaultTableModel();
		mower_table.setColumnIdentifiers(statusColumnNames);
		mo_table.setModel(mower_table);
		
		scrollPane.setViewportView(mo_table);
	}
}