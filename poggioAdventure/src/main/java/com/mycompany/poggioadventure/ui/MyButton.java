package com.mycompany.poggioadventure.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

public class MyButton extends JButton {

    private boolean over;
    private Color color;
    private Color colorOver;
    private Color colorClick;
    private Color borderColor;
    private int radius = 0;

    private Color textColor;
    private Color textColorOver;

    public MyButton() {
        //  Init Color
        setColor(Color.WHITE);
        colorOver = new Color(179, 250, 160);
        colorClick = new Color(152, 184, 144);
        borderColor = new Color(30, 136, 56);
        textColor = Color.WHITE;
        textColorOver = Color.WHITE; // Set default text color to white when mouse is over
        setContentAreaFilled(false);
        setForeground(textColor); // Set initial text color

        //  Add event mouse
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent me) {
                setBackground(colorOver);
                setForeground(textColorOver); // Change text color to white when mouse is over
                over = true;
            }

            @Override
            public void mouseExited(MouseEvent me) {
                setBackground(color);
                setForeground(textColor); // Reset text color when mouse exits
                over = false;
            }

            @Override
            public void mousePressed(MouseEvent me) {
                setBackground(colorClick);
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                if (over) {
                    setBackground(colorOver);
                    setForeground(textColorOver); // Ensure text color is correct
                } else {
                    setBackground(color);
                    setForeground(textColor); // Reset text color
                }
            }
        });
    }

    // Getter and Setter for textColor
    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
        setForeground(textColor); // Update text color immediately
    }

    // Getter and Setter for textColorOver
    public Color getTextColorOver() {
        return textColorOver;
    }

    public void setTextColorOver(Color textColorOver) {
        this.textColorOver = textColorOver;
    }

    // Other existing getters and setters
    public boolean isOver() {
        return over;
    }

    public void setOver(boolean over) {
        this.over = over;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        setBackground(color);
    }

    public Color getColorOver() {
        return colorOver;
    }

    public void setColorOver(Color colorOver) {
        this.colorOver = colorOver;
    }

    public Color getColorClick() {
        return colorClick;
    }

    public void setColorClick(Color colorClick) {
        this.colorClick = colorClick;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    @Override
    protected void paintComponent(Graphics grphcs) {
        Graphics2D g2 = (Graphics2D) grphcs;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //  Paint Border
        g2.setColor(borderColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        g2.setColor(getBackground());
        //  Border set 2 Pix
        g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, radius, radius);
        
        // No call to super.paintComponent to ensure custom paint behavior
        g2.setColor(getForeground()); // Set the text color
        super.paintComponent(grphcs);
    }
}
