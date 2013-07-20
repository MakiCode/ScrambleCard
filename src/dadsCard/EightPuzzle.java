package src.dadsCard;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * An Eight Puzzle game where you can click on blocks and they slide into an
 * empty slot if it is next to them. The goal is to organize all of the blocks.
 * 
 * @author Huulktya
 * 
 */
@SuppressWarnings("serial")
public class EightPuzzle extends JPanel {

	private int currentFrameWidth;
	private int currentFrameHeight;
	private int sideWidth;
	private Map<Integer, Image> imageMap;
	private Rectangle nextMove;
	private Rectangle scramble;
	private int[][] gameBoard;
	private boolean clickingNext = false;
	private boolean clickingScramble = false;
	private int diffHeightWidth;
	private int sideHeight;
	private int numOfTilesOnSide;
	private int numOfFrames = 30;
	private final Dimension nextMoveStringDim = new Dimension(66, 10);
	private final Dimension scrambleStringDim = new Dimension(55, 10);
	private JPopupMenu nextMovePopUp;

	public static void main(final String[] args) {
		EightPuzzle eightPuzzle = new EightPuzzle(450, 550, 3);
		eightPuzzle.createFrame();
	}

	/**
	 * Create the frame and put this panel in it and GO!
	 */
	private void createFrame() {
		JFrame frame = new JFrame();
		frame.setSize(new Dimension(currentFrameWidth, currentFrameHeight));
		Point position = getCenterPosition(frame.getSize(), Toolkit
				.getDefaultToolkit().getScreenSize());
		frame.setLocation((int) position.getX(), (int) position.getY());

		frame.add(this);
		frame.addComponentListener(getComponentListener());
		createPopUps();

		frame.setResizable(true);
		frame.setVisible(true);
	}

	private void createPopUps() {
		nextMovePopUp = new JPopupMenu();
		JMenuItem menuItem = new JMenuItem(
				"Clicking this will cause the computer to decide the next move.");
		nextMovePopUp.add(menuItem);
		nextMovePopUp.addMouseListener(new NextMovePopupListener());

	}

	private class NextMovePopupListener extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON3) {
				if (nextMove.contains(e.getX(), e.getY())) {
					if (e.isPopupTrigger()) {
						nextMovePopUp
								.show(EightPuzzle.this, e.getX(), e.getY());
					}
				}
			}
		}
	}

	/**
	 * Activate the animation loop. This Method runs forever so be careful.
	 */
	public void animate() {
		for (int i = 0; i < numOfFrames; i++) {
			repaint();
			System.out.println("Animating");
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Get the Component listener for this EightPuzzle.
	 * 
	 * @return A component listener that only handles resize events
	 */
	private ComponentListener getComponentListener() {
		return new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				Dimension currentSize = e.getComponent().getSize();
				makeVars(currentSize.width, currentSize.height,
						numOfTilesOnSide);
			}
		};
	}

	/**
	 * Create an Instance of EightPuzzle in a JPanel.
	 * 
	 * @param frameWidth
	 *            The Width of the Frame this game will be put into
	 * @param frameHeight
	 *            The Height of the Frame this game will be put into
	 * @param numOfTilesOnSideVal
	 *            The number of tiles on one side of the game
	 */
	public EightPuzzle(final int frameWidth, final int frameHeight,
			int numOfTilesOnSideVal) {

		makeVars(frameWidth, frameHeight, numOfTilesOnSideVal);
		addMouseListener(new MyMouseListener());
		generateMap();
		scramble();
	}

	/**
	 * Redefine the variables pertaining to the screen dimensions
	 * 
	 * @param frameWidth
	 *            the width of the frame
	 * @param frameHeight
	 *            the height of the frame
	 */
	private void makeVars(final int frameWidth, final int frameHeight,
			int numOfTilesOnSideVal) {
		numOfTilesOnSide = numOfTilesOnSideVal;
		currentFrameWidth = frameWidth;
		currentFrameHeight = frameHeight;
		diffHeightWidth = frameHeight - frameWidth;
		sideWidth = currentFrameWidth / numOfTilesOnSideVal;
		sideHeight = (currentFrameHeight - diffHeightWidth)
				/ numOfTilesOnSideVal;

		int buttonWidth = (currentFrameWidth / 3) / 2;
		int buttonHeight = diffHeightWidth / 2;
		if (buttonHeight <= 40) {
			buttonHeight = 40;
		}

		int yOfButtonsFromBot = 70;
		int spacing = (sideWidth / 15);
		int buttonY = currentFrameHeight - diffHeightWidth
				+ ((diffHeightWidth / 3) / 2);
		if (buttonY >= frameHeight - yOfButtonsFromBot) {
			buttonY = frameHeight - yOfButtonsFromBot;
		}

		int firstButtonX = currentFrameWidth / 3;

		nextMove = new Rectangle(firstButtonX, buttonY, buttonWidth,
				buttonHeight);
		scramble = new Rectangle(firstButtonX + buttonWidth + spacing, buttonY,
				buttonWidth, buttonHeight);

		if (gameBoard == null) {
			gameBoard = new int[numOfTilesOnSideVal][numOfTilesOnSideVal];
			int n = 1;
			for (int i = 0; i < numOfTilesOnSideVal; i++) {
				for (int j = 0; j < numOfTilesOnSideVal; j++) {
					if (i == (numOfTilesOnSideVal - 1)
							&& j == (numOfTilesOnSideVal - 1)) {
						gameBoard[i][j] = 0;
					} else {
						gameBoard[i][j] = n;
					}
					n++;
				}
			}
		}
	}

	@Override
	public final void paint(final Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		for (int rows = 0; rows < gameBoard.length; rows++) {
			for (int columns = 0; columns < gameBoard[rows].length; columns++) {
				// potential spot for animation
				int x = columns * sideWidth;
				int y = rows * sideHeight;
				g.drawImage(
						resizeImage((BufferedImage) imageMap
								.get(gameBoard[rows][columns]), sideHeight,
								sideWidth), x, y, null);
				g2d.setColor(Color.BLACK);
				g2d.drawRect(x, y, sideWidth, sideHeight);
			}
		}
		// Fills in the gray at where the buttons are
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.fillRect(0, currentFrameHeight - diffHeightWidth,
				currentFrameWidth, diffHeightWidth);
		// Draws an outline around the area where the buttons are
		g2d.setColor(Color.BLACK);
		g2d.drawRect(0, currentFrameHeight - diffHeightWidth,
				currentFrameWidth, diffHeightWidth);

		// Draw the buttons
		g2d.setColor(Color.CYAN);
		g2d.fill3DRect(nextMove.x, nextMove.y, nextMove.width, nextMove.height,
				!clickingNext);
		g2d.fill3DRect(scramble.x, scramble.y, scramble.width, scramble.height,
				!clickingScramble);
		// and draw the strings onto the buttons
		g2d.setColor(Color.BLACK);
		Point scramblePos = getCenterPosition(scrambleStringDim,
				scramble.getSize());
		Point nextMovePos = getCenterPosition(nextMoveStringDim,
				nextMove.getSize());
		g2d.drawString("Next Move", nextMove.x + nextMovePos.x, nextMove.y
				+ nextMovePos.y + nextMoveStringDim.height);
		g2d.drawString("Scramble", scramble.x + scramblePos.x, scramble.y
				+ scramblePos.y + nextMoveStringDim.height);
	}

	/**
	 * Calculate the proper position (x,y) on the screen
	 * 
	 * @param rect
	 *            The rectangle that will be placed in the center of the
	 *            backgorund
	 * @param background
	 *            the rectangle that the rect will be placed onto
	 * @return the correct x,y position on the screen
	 */
	static Point getCenterPosition(final Dimension rect,
			final Dimension background) {
		if (rect.getWidth() > background.getWidth()
				|| rect.getHeight() > background.getHeight()) {
			throw new ArithmeticException(
					"background was too small for the rect!");
		} else {
			int x = background.width / 2 - rect.getSize().width / 2;
			int y = background.height / 2 - rect.getSize().height / 2;
			return new Point(x, y);
		}
	}

	/**
	 * Resize the image to a given width and height
	 * 
	 * @param originalImage
	 *            the image to resize
	 * @param height
	 *            the height of the resized image
	 * @param width
	 *            the width of the resized image
	 * @return the original image resized to the given dimensions
	 */
	private static BufferedImage resizeImage(final BufferedImage originalImage,
			int width, int height) {
		BufferedImage resizedImage = new BufferedImage(width, height,
				originalImage.getType());
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, width, height, null);
		g.dispose();
		return resizedImage;
	}

	/**
	 * Generate the hashmap that represents the connection between the 2d array
	 * and the images that data refers too
	 */
	private void generateMap() {
		imageMap = new HashMap<Integer, Image>();
		BufferedImage img = null;
		try {
			img = getImg("image.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
		img = resizeImage(img, currentFrameWidth, currentFrameHeight
				- diffHeightWidth);

		Graphics g = img.getGraphics();
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect((numOfTilesOnSide - 1) * sideWidth, (numOfTilesOnSide - 1)
				* sideHeight, sideWidth, sideHeight);
		g.dispose();

		imageMap = splitImg(img, numOfTilesOnSide);
	}

	/**
	 * Split up the image into tiles numbered 1-(numOfTilesOnSide^2 - 1) with
	 * the bottom right tile being numbered 0
	 * 
	 * @param img
	 *            The image to split up
	 * @param numOfTilesOnSide
	 *            the number of tiles on one side of the image
	 * @return a map of the tiles of the image to the numbers as specified above
	 */
	private Map<Integer, Image> splitImg(BufferedImage img, int numOfTilesOnSide) {
		Map<Integer, Image> map = new HashMap<>();

		int sideWidth2 = img.getWidth() / numOfTilesOnSide;
		int sideHeight2 = img.getHeight() / numOfTilesOnSide;

		int n = 1;
		for (int i = 0; i < numOfTilesOnSide; i++) {
			for (int j = 0; j < numOfTilesOnSide; j++) {
				if (i == (numOfTilesOnSide - 1) && j == (numOfTilesOnSide - 1)) {
					map.put(0, img.getSubimage(j * sideWidth2, i * sideHeight2,
							sideWidth2, sideHeight2));
				} else {
					map.put(n, img.getSubimage(j * sideWidth2, i * sideHeight2,
							sideWidth2, sideHeight2));
				}
				n++;
			}
		}
		return map;
	}

	/**
	 * Load the image at the specified fileName
	 * 
	 * @param fileName
	 *            the name of the file that has the image
	 * @return the image in fileName
	 * @throws IOException
	 *             if there is a problem in the FileIO
	 */
	private BufferedImage getImg(String fileName) throws IOException {
		BufferedImage img;
		URL url = this.getClass().getResource(fileName);
		if (url == null) {
			throw new IOException("Input was wrong");
		}
		img = ImageIO.read(url);
		return img;
	}

	/**
	 * Scramble the gameboard by making 25 random moves
	 */
	private void scramble() {
		Random randomGen = new Random();
		int loopVar = (int) (25 + Math.pow(numOfTilesOnSide, 2));
		for (int i = 0; i < loopVar; i++) {
			Board board = new Board(gameBoard);
			List<Board> boardNeighbors = board.neighbors();
			gameBoard = boardNeighbors.get(
					randomGen.nextInt(boardNeighbors.size())).getTiles();
		}

	}

	/**
	 * Find the proper best next move to make
	 */
	private void findNextMove() {
		Solver solver = new Solver(new Board(gameBoard));
		Stack<Board> stack = solver.solution();
		stack.pop();
		if (!stack.isEmpty()) {
			gameBoard = stack.pop().getTiles();
		}
	}

	/**
	 * Check that the puzzle in its "Completed" state
	 * 
	 * @returns true if the puzzle is completed false otherwise
	 */
	private boolean checkState() {
		Board board = new Board(gameBoard);
		if (board.hamming() == 0) {
			return true;
		}
		return false;
	}

	/**
	 * Display a JOptionPane and execute the proper action based on the users
	 * input
	 */
	private void showOptions() {
		int choice = JOptionPane.showConfirmDialog(this,
				"Puzzle complete.\nWould you like to retry the puzzle?");
		if (choice == 0) {
			scramble();
		}
		repaint();
	}

	/**
	 * Display a warning saying that the algorithm to solve the puzzle may take
	 * a long time, An infeasible long time
	 * 
	 * @return true if the player wants to continue the next move operation
	 */
	private boolean showWarning() {
		int choice = JOptionPane.showConfirmDialog(this,
				"WARNING: This puzzle is rather large.\n"
						+ "Finding the next move may take\n"
						+ "a VERY long time if the puzzle\n"
						+ "is heavily scrambled. Do you\n"
						+ "want to continue with the next\n"
						+ "move operation?");
		if (choice == 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Swap a position on the board with the coordinates of the zero value in
	 * the gameboard. All values are allowed but some are ignored.
	 * 
	 * @param x
	 *            the x coordinate of the tile that will be swapped
	 * @param y
	 *            the y coordinate of the tile that will be swapped
	 * @return true if swap succeeded
	 */
	private boolean swap(final int x, final int y) {
		if (!(x >= 3 || y >= 3 || x < 0 || y < 0)) {
			Point zeroCoords = getZeroCoords();
			if ((Math.abs(x - zeroCoords.x) == 1 ^ Math.abs(y - zeroCoords.y) == 1)) {
				if (Math.abs(y - zeroCoords.y) == 1
						&& (Math.abs(x - zeroCoords.x) == 0)) {
					swap(x, y, zeroCoords.x, zeroCoords.y);
					return true;
				} else {
					if (Math.abs(x - zeroCoords.x) == 1
							&& (Math.abs(y - zeroCoords.y) == 0)) {
						swap(x, y, zeroCoords.x, zeroCoords.y);
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 
	 * A helper function that manages 2d array swapping with two arbitrary
	 * points
	 * 
	 * @param x1
	 *            the first x coordinate
	 * @param y1
	 *            the first y coordinate
	 * @param x2
	 *            the second x coordinate
	 * @param y2
	 *            the second y coordinate
	 */
	private void swap(final int x1, final int y1, final int x2, final int y2) {
		int temp = gameBoard[y1][x1];
		gameBoard[y1][x1] = gameBoard[y2][x2];
		gameBoard[y2][x2] = temp;
	}

	/**
	 * get the coordinates of the zero value in the gameBoard
	 * 
	 * @return the coordinates of the zero in the gameBoard, null if it could
	 *         not find the zero
	 */
	private Point getZeroCoords() {
		for (int x = 0; x < gameBoard.length; x++) {
			for (int y = 0; y < gameBoard[x].length; y++) {
				if (gameBoard[y][x] == 0) {
					return new Point(x, y);
				}
			}
		}
		return null;
	}

	/**
	 * Handle clicking the next move button. Displays warning if needed.
	 */
	private void handleNext() {
		boolean doNextMove = true;
		if (numOfTilesOnSide >= 5) {
			Board board = new Board(gameBoard);
			if (board.hamming() > Math.pow(numOfTilesOnSide, 2) / 3) {
				if (!showWarning()) {
					doNextMove = false;
				}
			}
		}
		if (doNextMove) {
			findNextMove();
		}
		repaint();
	}

	private class MyMouseListener extends MouseAdapter {
		@Override
		public final void mousePressed(final MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				if (nextMove.contains(new Point(e.getX(), e.getY()))) {
					clickingNext = true;
					repaint();
				} else if (scramble.contains(new Point(e.getX(), e.getY()))) {
					clickingScramble = true;
					repaint();
				}
			}
		}

		@Override
		public final void mouseReleased(final MouseEvent e) {
			if (clickingNext) {
				handleNext();
			} else if (clickingScramble) {
				scramble();
				repaint();
			} else if (e.getButton() == MouseEvent.BUTTON1) {
				if (swap(e.getX() / sideWidth, e.getY() / sideWidth)) {
					repaint();
				}
			}
			if (checkState()) {
				showOptions();
			}
			clickingNext = false;
			clickingScramble = false;
		}

	}

}
