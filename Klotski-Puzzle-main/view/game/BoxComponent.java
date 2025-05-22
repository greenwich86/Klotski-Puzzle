package view.game;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
public class BoxComponent extends JComponent {
    private Color color;
    private int row;
    private int col;
    private boolean isSelected;
    private boolean movable;
    private boolean isAnimating;
    private Image pieceImage;
    private boolean isImageLoaded = false;

    public BoxComponent(Color color, int row, int col) {
        this(color, row, col, true);
    }

    public BoxComponent(Color color, int row, int col, boolean movable) {
        this.color = color;
        this.row = row;
        this.col = col;
        this.isSelected = false;
        this.movable = movable;
    }

    private void loadAndScaleImage() {
        if (isImageLoaded || getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        String imagePath = getImagePathByColor();
        if (imagePath == null) {
            isImageLoaded = true;
            return;
        }

        java.io.InputStream imageStream = getClass().getResourceAsStream(imagePath);
        if (imageStream == null) {
            System.err.println("图片资源不存在，路径：" + imagePath);
            isImageLoaded = true;
            return;
        }

        try (imageStream) {
            BufferedImage originalImage = ImageIO.read(imageStream);
            if (originalImage != null) {
                pieceImage = originalImage.getScaledInstance(
                        getWidth(),
                        getHeight(),
                        Image.SCALE_SMOOTH
                );
                isImageLoaded = true;
            } else {
                System.err.println("无法读取图片格式，路径：" + imagePath);
                isImageLoaded = true;
            }
        } catch (IOException e) {
            System.err.println("图片读取失败，路径：" + imagePath + "，错误：" + e.getMessage());
            isImageLoaded = true;
        }
    }

    private String getImagePathByColor() {
        if (color.equals(Color.RED)) {
            return "/resource/Cao Cao.jpg";
        } else if (color.equals(Color.ORANGE)) {
            return "/resource/Guan Yu.jpg";
        } else if (color.equals(Color.BLUE)) {
            return "/resource/General.jpg";
        } else if (color.equals(Color.GREEN)) {
            return "/resource/Soldier.jpg";
        } else if (color.equals(Color.MAGENTA)) {
            return "/resource/Zhou Yu.jpg";
        } else if (color.equals(Color.DARK_GRAY)) {
            return "/resource/Blocked.jpg";
        }
        return null;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!isImageLoaded) {
            loadAndScaleImage();
        }

        if (pieceImage != null) {
            g.drawImage(pieceImage, 0, 0, this);
        } else {
            g.setColor(color);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        g.setColor(Color.RED);
        g.drawRect(0, 0, getWidth()-1, getHeight()-1);

        Border border;
        if(isSelected){
            border = BorderFactory.createLineBorder(Color.red,3);
        }else {
            border = BorderFactory.createLineBorder(Color.DARK_GRAY, 1);
        }
        this.setBorder(border);
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        this.repaint();
    }

    public Color getColor() {
        return this.color;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public boolean isAnimating() {
        return isAnimating;
    }

    public void setAnimating(boolean animating) {
        this.isAnimating = animating;
    }

    public boolean isMovable() {
        return movable;
    }

    public void setMovable(boolean movable) {
        this.movable = movable;
        this.repaint();
    }
}