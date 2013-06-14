package com.migapro.sudokusolver;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Sudoku program that validates and finds a solution.
 * @author Miga
 * @version 1.1
 *
 */
public class SudokuSolver extends JFrame implements ActionListener {

	/** UID */
	private static final long serialVersionUID = 3883151525928534467L;

	/** Contains UI for cells */
	private SudokuCell[][] sudokuCells;
	/** Contains integers representing values in cells. */
	private int[][] cellValues;

	/**
	 * Constructor.
	 * Set up the components and initialize Sudoku.
	 */
	public SudokuSolver() {
		super("Sudoku Solver");
		
		prepareSudokuUI();
		
		// JFrame property
		setLayout(new FlowLayout(FlowLayout.CENTER));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((d.width / 2 - 175), (d.height / 2 - 275));
		setResizable(false);
		setVisible(true);
	}
	
	/**
	 * Set up the UI for application.
	 */
	private void prepareSudokuUI() {
		// Used to align the title, Sudoku grid, and button panels vertically
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		// Title panel
		JPanel title = new JPanel();
		title.add(new JLabel(new ImageIcon(getClass().getResource("/resources/title.png"))));
		
		// Sudoku grid panel
		JPanel sudokuPanel = new JPanel();
		sudokuPanel.setLayout(new GridLayout(3, 3, 1, 1));

		// Set up 9 3x3 box panels
		JPanel[] boxes = new JPanel[9];
		boxes = prepare3x3BoxUI(sudokuPanel, boxes);
		
		// Set up SudokuCells
		cellValues = new int[9][9];
		sudokuCells = new SudokuCell[9][9];
		prepareSudokuCellsUI(boxes);

		// Bottom part containing buttons
		// First row of buttons
		JPanel buttonsPanel = new JPanel();
		JButton submitButton = new JButton("Submit"); // Submit to validate the Sudoku
		JButton solveButton = new JButton("Solve"); // Solve the Sudoku
		JButton eraseButton = new JButton("Erase"); // Clear the cells that are not fixed
		JButton eraseAllButton = new JButton("Erase All"); // Clear all cells including fixed ones
		
		buttonsPanel.add(submitButton);
		buttonsPanel.add(solveButton);
		buttonsPanel.add(eraseButton);
		buttonsPanel.add(eraseAllButton);
		
		// Second row of buttons
		JPanel buttonsPanel2 = new JPanel();
		JButton presetButton = new JButton("Mark As Preset"); // Set filled cells as preset (not editable)
		
		buttonsPanel2.add(presetButton);
		
		submitButton.addActionListener(this);
		solveButton.addActionListener(this);
		presetButton.addActionListener(this);
		eraseButton.addActionListener(this);
		eraseAllButton.addActionListener(this);
		
		panel.add(title);
		panel.add(sudokuPanel);
		panel.add(buttonsPanel);
		panel.add(buttonsPanel2);
		add(panel);
	}
	
	/**
	 * Set up 9 3x3 panels for Sudoku panel.
	 * Use 9 panels to align as 3x3 boxes.
	 * @param sudokuPanel 
	 * @param boxes 3x3 box panels to be added on Sudoku panel.
	 * @return instantiated 3x3 box panels.
	 */
	private JPanel[] prepare3x3BoxUI(JPanel sudokuPanel, JPanel[] boxes) {
		for (int i = 0; i < 9; i++) {
			boxes[i] = new JPanel();
			boxes[i].setLayout(new GridLayout(3, 3, 0, 0));
			boxes[i].setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
			sudokuPanel.add(boxes[i]);
		}
		
		return boxes;
	}
	
	/**
	 * Set up cells UI(SudokuCell) for input and add them to the panels.
	 * SudokuCells have to be added to proper panels meaning left-right and top-bottom order within 3x3 box.
	 * @param boxes
	 */
	private void prepareSudokuCellsUI(JPanel[] boxes) {
		int index = 0;
		
		// Adjust current row
		for (int i = 0; i < 9; i++) {
			if (i <= 2)
				index = 0;
			else if (i <= 5)
				index = 3;
			else
				index = 6;
			
			for (int j = 0; j < 9; j++) {
				sudokuCells[i][j] = new SudokuCell(i, j);
				boxes[index + (j / 3)].add(sudokuCells[i][j]);
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		JButton button = (JButton) event.getSource();
		String buttonType = button.getText();
		
		if (buttonType.equals("Submit"))
			submitSudoku();
		else if (buttonType.equals("Solve"))
			startSolving();
		else if (buttonType.equals("Erase"))
			erase();
		else if (buttonType.equals("Erase All"))
			eraseAllIncludingPresetCells();
		else
			checkPresetCells();
	}
	
	/**
	 * Submit current Sudokue to validate for completeness.
	 */
	private void submitSudoku() {
		if (isSudokuSolved())
			JOptionPane.showMessageDialog(getRootPane(), 
					"<html><center>Congratulations!<br>Sudoku has been Completed!</center></html>", 
					"Sudoku Validation", JOptionPane.INFORMATION_MESSAGE);
		else
			JOptionPane.showMessageDialog(getRootPane(), 
					"<html><center>Failed!<br>Sudoku is not complete!</center></html>", 
					"Sudoku Validation", JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * Start working on the Sudoku if it is ready.
	 */
	private void startSolving() {
		// Don't do anything if Sudoku is already full
		if (isSudokuFull()) {
			JOptionPane.showMessageDialog(getRootPane(), 
					"<html><center>There are no cells open to start from.</center></html>", 
					"Solving Sudoku", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		// Validate before starting
		if (isValidToStart()) {
			markAsPresetCells();
			if (!solve(0, 0))
				JOptionPane.showMessageDialog(getRootPane(), 
						"<html><center>Unable to solve.</center></html>", 
						"Solving Sudoku", JOptionPane.ERROR_MESSAGE);
		} else // Don't start solving if Sudoku is not valid at the start
			JOptionPane.showMessageDialog(getRootPane(), 
					"<html><center>This is not a valid Sudoku to start.</center></html>", 
					"Solving Sudoku", JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Erase all editable cells.
	 */
	private void erase() {
		for (int i = 0; i < 9; i++)
			for (int j = 0; j < 9; j++) {
				if (cellValues[i][j] != 0) {
					if (sudokuCells[i][j].editable) {
						sudokuCells[i][j].setText("");
						cellValues[i][j] = 0;
					}
				}
			}
	}
	
	/**
	 * Erase all cell values including those with fixed values.
	 */
	private void eraseAllIncludingPresetCells() {
		for (int i = 0; i < 9; i++)
			for (int j = 0; j < 9; j++) {
				if (cellValues[i][j] != 0) {
					if (!sudokuCells[i][j].editable) {
						sudokuCells[i][j].editable = true;
						sudokuCells[i][j].setEditable(true);
						sudokuCells[i][j].setForeground(Color.BLACK);
					}
					sudokuCells[i][j].setText("");
					cellValues[i][j] = 0;
				}
			}
	}
	
	/**
	 * Evaluate cells before making pre filled cells fixed.
	 */
	private void checkPresetCells() {
		// Validate the current state and make the filled cells fixed
		if (!isValidToStart())
			JOptionPane.showMessageDialog(getRootPane(), 
					"<html><center>This is not a valid Sudoku to start.</center></html>", 
					"Sudoku Solver", JOptionPane.ERROR_MESSAGE);
		else
			markAsPresetCells();
	}
	
	/**
	 * Check if the Sudoku puzzle is solved correctly.
	 * @return true if Sudoku is correct.
	 */
	private boolean isSudokuSolved() {
		for (int i = 0; i < 9; i++) {
			int[] aRow = new int[9];
			int[] aCol = new int[9];
			
			for (int j = 0; j < 9; j++) {
				// If this cell is empty, quit because it's not complete
				if (cellValues[i][j] == 0)
					return false;
				
				aRow[j] = cellValues[i][j];
				aCol[j] = cellValues[j][i];
				
				// Check if the value in this cell is duplicated in 3x3 box
				if (isContainedIn3x3Box(i, j, cellValues[i][j]))
					return false;
			}
			
			// Check rows and columns
			if (!isRowColumnCorrect(aRow, aCol))
				return false;
		}
		
		return true;
	}
	
	/**
	 * Check if specified row and column are correct.
	 * Used when submitting the puzzle.
	 * @param aRow 9 numbers in a row.
	 * @param aCol 9 numbers in a column.
	 * @return true if this row and column are correct.
	 */
	private boolean isRowColumnCorrect(int[] aRow, int[] aCol) {
		Arrays.sort(aRow);
		Arrays.sort(aCol);
		
		for (int i = 0; i < 9; i++)
			if (aRow[i] != i + 1 && aCol[i] != i + 1)
				return false;
		
		return true;
	}
	
	/**
	 * Check if Sudoku is in valid condition to start.
	 * @return true if Sudoku is ready to start.
	 */
	private boolean isValidToStart() {
		for (int i = 0; i < 9; i++)
			for (int j = 0; j < 9; j++)
				if (cellValues[i][j] != 0)
					if (isContainedIn3x3Box(i, j, cellValues[i][j]) ||
							isContainedInRowColumn(i, j, cellValues[i][j]))
						return false;
		
		return true;
	}
	
	/**
	 * Make the filled cells fixed.
	 */
	private void markAsPresetCells() {
		for (int i = 0; i < 9; i++)
			for (int j = 0; j < 9; j++)
				if (cellValues[i][j] != 0)
					if (!isContainedIn3x3Box(i, j, cellValues[i][j]) &&
							!isContainedInRowColumn(i, j, cellValues[i][j])) {
						sudokuCells[i][j].editable = false;
						sudokuCells[i][j].setEditable(false);
						sudokuCells[i][j].setForeground(new Color(150, 150, 150));
					}
	}
	
	/**
	 * Check if a value contains in its 3x3 box for a cell.
	 * @param row current row index.
	 * @param col current column index.
	 * @return true if this cell is incorrect or duplicated in its 3x3 box.
	 */
	private boolean isContainedIn3x3Box(int row, int col, int value) {
		// Find the top left of its 3x3 box to start validating from
		int startRow = row / 3 * 3;
		int startCol = col / 3 * 3;
		
		// Check within its 3x3 box except its cell
		for (int i = startRow; i < startRow + 3; i++)
			for (int j = startCol; j < startCol + 3; j++)
				if (!(i == row && j == col))
					if (cellValues[i][j] == value)
						return true;

		return false;
	}
	
	/**
	 * Check if a value is contained within its row and column.
	 * Used when solving the puzzle.
	 * @param row current row index.
	 * @param col current column index.
	 * @param value value in this cell.
	 * @return true if this value is duplicated in its row and column.
	 */
	private boolean isContainedInRowColumn(int row, int col, int value) {
		for (int i = 0; i < 9; i++) {
			// Don't check the same cell
			if (i != col)
				if (cellValues[row][i] == value)
					return true;
			if (i != row)
				if (cellValues[i][col] == value)
					return true;
		}
		
		return false;
	}
	
	/**
	 * Check if all cells are filled up.
	 * @return true if Sudoku is full.
	 */
	private boolean isSudokuFull() {
		for (int i = 0; i < 9; i++)
			for (int j = 0; j < 9; j++)
				if (cellValues[i][j] == 0)
					return false;
		
		return true;
	}
	
	/**
	 * Solve Sudoku recursively.
	 * @param row current row index.
	 * @param col current column index.
	 * @return false if Sudoku is not solved. true if Sudoku is solved.
	 */
	private boolean solve(int row, int col) {
		// If it has passed through all cells, start quitting
		if (row == 9)
			return true;
		
		// If this cell is already set(fixed), skip to the next cell
		if (cellValues[row][col] != 0) {
			if (solve(col == 8? (row + 1): row, (col + 1) % 9))
				return true;
		} else {
			// Random numbers 1 - 9
			Integer[] randoms = generateRandomNumbers();
			for (int i = 0; i < 9; i++) {
				
				// If no duplicates in this row, column, 3x3, assign the value and go to the next
				if (!isContainedInRowColumn(row, col, randoms[i]) &&
						!isContainedIn3x3Box(row, col, randoms[i])) {
					cellValues[row][col] = randoms[i];
					sudokuCells[row][col].setText(String.valueOf(randoms[i]));
					
					// Move to the next cell left-to-right and top-to-bottom
					if (solve(col == 8? (row + 1) : row, (col + 1) % 9))
						return true;
					else { 
						// Initialize the cell when backtracking (case when the value in the next cell was not valid)
						cellValues[row][col] = 0;
						sudokuCells[row][col].setText("");
					}
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Generate 9 unique random numbers.
	 * @return array containing 9 random unique numbers.
	 */
	private Integer[] generateRandomNumbers() {
		ArrayList<Integer> randoms = new ArrayList<Integer>();
		for (int i = 0; i < 9; i++)
			randoms.add(i + 1);
		Collections.shuffle(randoms);
		
		return randoms.toArray(new Integer[9]);
	}
	
	/**
	 * A SudokuCell represents a cell for input.
	 * @author Miga
	 *
	 */
	private class SudokuCell extends JTextField {

		/** UID */
		private static final long serialVersionUID = 4690751052748480438L;
		
		/** Determine if this cell can accept input. */
		private boolean editable;

		/**
		 * Constructor
		 * @param row index for row of this cell.
		 * @param col index for column of this cell.
		 */
		public SudokuCell(final int row, final int col) {
			super(1);
			
			editable = true;
			
			setBackground(Color.WHITE);
			setBorder(BorderFactory.createLineBorder(Color.GRAY));
			setHorizontalAlignment(CENTER);
			setPreferredSize(new Dimension(35, 35));
			setFont(new Font("Lucida Console", Font.BOLD, 28));
			
			addFocusListener(new FocusListener(){

				@Override
				public void focusGained(FocusEvent arg0) {
					// Change colors of fields located in vertical, horizontal, and 3x3 fields
					int startRow = row / 3 * 3;
					int startCol = col / 3 * 3;

					for (int i = 0; i < 9; i++) {
						// Horizontal
						sudokuCells[i][col].setBackground(new Color(255, 227, 209));
						// Vertical
						sudokuCells[row][i].setBackground(new Color(255, 227, 209));
					}

					// 3x3 box
					for (int i = startRow; i < startRow + 3; i++)
						for (int j = startCol; j < startCol + 3; j++)
							sudokuCells[i][j].setBackground(new Color(255, 227, 209));
				}

				@Override
				public void focusLost(FocusEvent arg0) {
					// Set the previous color of fields back to white
					int startRow = row / 3 * 3;
					int startCol = col / 3 * 3;
					
					// Reset focus (set background color back to white)
					for (int i = 0; i < 9; i++) {
						// Horizontal
						sudokuCells[i][col].setBackground(Color.WHITE);
						// Vertical
						sudokuCells[row][i].setBackground(Color.WHITE);
					}
					
					// 3x3 box
					for (int i = startRow; i < startRow + 3; i++)
						for (int j = startCol; j < startCol + 3; j++)
							sudokuCells[i][j].setBackground(Color.WHITE);
				}
				
			});
			
			addKeyListener(new KeyAdapter() {
				
				@Override
				public void keyPressed(KeyEvent e) {
					// Only allow numeric input
					if (editable)
						if (e.getKeyChar() >= '1' && e.getKeyChar() <= '9') {
							setEditable(true);
							setText(""); // Keep it 1 letter
							cellValues[row][col] = e.getKeyChar() - 48;
						} else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
							setEditable(true);
							setText("0"); // Avoid beep sound
							cellValues[row][col] = 0;
						} else
							setEditable(false);
					
					// Navigation by arrow keys
					switch (e.getKeyCode()) {
					case KeyEvent.VK_DOWN:
						sudokuCells[(row + 1) % 9][col].requestFocusInWindow();
						break;
					case KeyEvent.VK_RIGHT:
						sudokuCells[row][(col + 1) % 9].requestFocusInWindow();
						break;
					case KeyEvent.VK_UP:
						sudokuCells[(row == 0)? 8 : (row - 1)][col].requestFocusInWindow();
						break;
					case KeyEvent.VK_LEFT:
						sudokuCells[row][(col == 0)? 8 : (col - 1)].requestFocusInWindow();
						break;
					}
				}
			});
		}

	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new SudokuSolver();
	}

}
