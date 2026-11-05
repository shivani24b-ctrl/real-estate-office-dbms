import java.awt.*;

public class Theme {
    public static final Color BG          = new Color(0xF7F8FA);
    public static final Color SURFACE     = Color.WHITE;
    public static final Color BORDER      = new Color(0xE2E5EA);
    public static final Color PRIMARY     = new Color(0x2563EB);
    public static final Color PRIMARY_DIM = new Color(0xDBEAFE);
    public static final Color SUCCESS     = new Color(0x16A34A);
    public static final Color SUCCESS_DIM = new Color(0xDCFCE7);
    public static final Color DANGER      = new Color(0xDC2626);
    public static final Color DANGER_DIM  = new Color(0xFEE2E2);
    public static final Color AMBER       = new Color(0xD97706);
    public static final Color AMBER_DIM   = new Color(0xFEF3C7);
    public static final Color TEXT        = new Color(0x111827);
    public static final Color TEXT_MID    = new Color(0x6B7280);
    public static final Color TEXT_LIGHT  = new Color(0x9CA3AF);
    public static final Color HEADER_BG   = new Color(0x1E3A5F);
    public static final Color HEADER_TEXT = Color.WHITE;

    public static final Font TITLE   = new Font("SansSerif", Font.BOLD,   22);
    public static final Font HEADING = new Font("SansSerif", Font.BOLD,   15);
    public static final Font BODY    = new Font("SansSerif", Font.PLAIN,  13);
    public static final Font SMALL   = new Font("SansSerif", Font.PLAIN,  11);
    public static final Font LABEL   = new Font("SansSerif", Font.BOLD,   12);
    public static final Font STAT    = new Font("SansSerif", Font.BOLD,   26);

    public static javax.swing.border.Border cardBorder() {
        return javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(BORDER, 1),
            javax.swing.BorderFactory.createEmptyBorder(16, 18, 16, 18)
        );
    }

    public static javax.swing.border.Border emptyBorder(int v, int h) {
        return javax.swing.BorderFactory.createEmptyBorder(v, h, v, h);
    }
}
