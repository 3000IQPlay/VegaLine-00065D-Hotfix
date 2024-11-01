package ru.govno.client.newfont;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import ru.govno.client.module.modules.ClientColors;
import ru.govno.client.module.modules.NameSecurity;
import ru.govno.client.utils.Math.MathUtils;
import ru.govno.client.utils.Render.ColorUtils;
import ru.govno.client.utils.Render.RenderUtils;

public class CFontRenderer extends CFont {
    protected CFont.CharData[] boldChars = new CFont.CharData[167];
    protected CFont.CharData[] italicChars = new CFont.CharData[167];
    protected CFont.CharData[] boldItalicChars = new CFont.CharData[167];
    private final float[] charWidthFloat = new float[256];
    private final byte[] glyphWidth = new byte[65536];
    private final int[] charWidth = new int[256];
    private final int[] colorCode = new int[32];
    private final String colorcodeIdentifiers = "0123456789abcdefklmnor";
    protected DynamicTexture texBold;
    protected DynamicTexture texItalic;
    protected DynamicTexture texItalicBold;

    public CFontRenderer(Font font, boolean antiAlias, boolean fractionalMetrics) {
        super(font, antiAlias, fractionalMetrics);
        this.setupMinecraftColorcodes();
        this.setupBoldItalicIDs();
    }

    public float drawNoBSString(String text, double d, float y2, int color) {
        text = text.replaceAll("Ã‚", "");
        return this.drawString(text, d, (double)y2, color, false);
    }

    public int drawPassword(String text, double d, float y2, int color) {
        text = text.replaceAll("Ã‚", "");
        return 0;
    }

    public float drawStringWithShadow(String text, double x, double y, int color) {
        text = NameSecurity.replacedIfActive(text);
        float shadowWidth = this.drawString(text, x + 0.5, y + 0.5, color, true);
        return Math.max(shadowWidth, this.drawString(text, x, y, color, false));
    }

    public float drawString(String text, double x, double y, int color) {
        text = NameSecurity.replacedIfActive(text);
        return this.drawString(text, x, y, color, false);
    }

    public void drawVGradientString(String text, double x, double y, int color, int color2) {
        GL11.glEnable(3089);

        for (double newY = y - 0.5; newY < y + (double)this.getHeight() + 3.0; newY += 0.5) {
            RenderUtils.scissor(0.0, (double)((float)newY - 1.0F), 100000.0, 0.5);
            GL11.glTranslated(x, y, 0.0);
            this.drawString(
                    text,
                    0.0,
                    0.0,
                    ColorUtils.getOverallColorFrom(color, color2, (float)MathUtils.clamp((newY - y) / (double)((float)this.getHeight() + 3.0F), 0.0, 1.0))
            );
            GL11.glTranslated(-x, -y, 0.0);
        }

        GL11.glDisable(3089);
    }

    public void drawVHGradientString(String text, double x, double y, int color, int color2, int color3, int color4) {
        x -= (double)this.getStringWidth(text);
        float index = 0.0F;

        for (char c : text.toCharArray()) {
            int col1 = ColorUtils.getOverallColorFrom(color, color2, index / (float)this.getStringWidth(text));
            int col2 = ColorUtils.getOverallColorFrom(color4, color3, index / (float)this.getStringWidth(text));
            this.drawVGradientString(String.valueOf(c), (double)((float)this.getStringWidth(text) + index) + x, y, col1, col2);
            index += (float)this.getStringWidth(String.valueOf(c));
        }
    }

    public void drawHGradientString(String text, double x, double y, int color, int color2) {
        x -= (double)this.getStringWidth(text);
        float index = 0.0F;

        for (char c : text.toCharArray()) {
            int col1 = ColorUtils.getOverallColorFrom(color, color2, index / (float)this.getStringWidth(text));
            this.drawString(String.valueOf(c), (double)((float)this.getStringWidth(text) + index) + x, y, col1);
            index += (float)this.getStringWidth(String.valueOf(c));
        }
    }

    public void drawClientColoredString(String text, double x, double y, float alphaPC, boolean shadow) {
        x -= (double)this.getStringWidth(text);
        float index = 0.0F;

        for (char c : text.toCharArray()) {
            int col1 = ColorUtils.getOverallColorFrom(
                    ClientColors.getColor1((int)index * 5), ClientColors.getColor2((int)index * 5), index / (float)this.getStringWidth(text)
            );
            col1 = ColorUtils.swapAlpha(col1, (float)ColorUtils.getAlphaFromColor(col1) * alphaPC);
            if (ColorUtils.getAlphaFromColor(col1) >= 32) {
                if (shadow) {
                    this.drawString(
                            String.valueOf(c),
                            (double)((float)this.getStringWidth(text) + index) + x + 0.5,
                            y + 0.5,
                            ColorUtils.swapDark(col1, ColorUtils.getBrightnessFromColor(col1) / 3.0F)
                    );
                }

                this.drawString(String.valueOf(c), (double)((float)this.getStringWidth(text) + index) + x, y, col1);
            }

            index += (float)this.getStringWidth(String.valueOf(c));
        }
    }

    public void drawClientColoredString(String text, double x, double y, float alphaPC, boolean shadow, int indexPlus) {
        x -= (double)this.getStringWidth(text);
        float index = 0.0F;

        for (char c : text.toCharArray()) {
            int col1 = ColorUtils.getOverallColorFrom(
                    ClientColors.getColor1((int)index * 5 + indexPlus), ClientColors.getColor2((int)index * 5 + indexPlus), index / (float)this.getStringWidth(text)
            );
            col1 = ColorUtils.swapAlpha(col1, (float)ColorUtils.getAlphaFromColor(col1) * alphaPC);
            if (ColorUtils.getAlphaFromColor(col1) >= 32) {
                if (shadow) {
                    this.drawString(
                            String.valueOf(c),
                            (double)((float)this.getStringWidth(text) + index) + x + 0.5,
                            y + 0.5,
                            ColorUtils.swapDark(col1, ColorUtils.getBrightnessFromColor(col1) / 1.5F)
                    );
                }

                this.drawString(String.valueOf(c), (double)((float)this.getStringWidth(text) + index) + x, y, col1);
            }

            index += (float)this.getStringWidth(String.valueOf(c));
        }
    }

    public void drawClientColoredString(String text, double x, double y, float alphaPC, boolean shadow, int indexPlus, boolean reverseColor) {
        x -= (double)this.getStringWidth(text);
        float index = 0.0F;

        for (char c : text.toCharArray()) {
            float pc = index / (float)this.getStringWidth(text);
            int col1 = ColorUtils.getOverallColorFrom(
                    ClientColors.getColor1((int)index * 5 + indexPlus), ClientColors.getColor2((int)index * 5 + indexPlus), reverseColor ? 1.0F - pc : pc
            );
            col1 = ColorUtils.swapAlpha(col1, (float)ColorUtils.getAlphaFromColor(col1) * alphaPC);
            if (ColorUtils.getAlphaFromColor(col1) >= 32) {
                if (shadow) {
                    this.drawString(
                            String.valueOf(c),
                            (double)((float)this.getStringWidth(text) + index) + x + 0.5,
                            y + 0.5,
                            ColorUtils.swapDark(col1, ColorUtils.getBrightnessFromColor(col1) / 1.5F)
                    );
                }

                this.drawString(String.valueOf(c), (double)((float)this.getStringWidth(text) + index) + x, y, col1);
            }

            index += (float)this.getStringWidth(String.valueOf(c));
        }
    }

    public void drawStringWithOutline(String text, double x, double y, int color) {
        int alpha = ColorUtils.getAlphaFromColor(color) / 3;
        this.drawString(text, x - 0.5, y - 0.5, ColorUtils.getColor(0, 0, 0, ColorUtils.getAlphaFromColor(color)), false);
        this.drawString(text, x + 0.5, y + 0.5, ColorUtils.getColor(0, 0, 0, ColorUtils.getAlphaFromColor(color)), false);
        this.drawString(text, x + 0.5, y, ColorUtils.getColor(0, 0, 0, ColorUtils.getAlphaFromColor(color)), false);
        this.drawString(text, x, y + 0.5, ColorUtils.getColor(0, 0, 0, ColorUtils.getAlphaFromColor(color)), false);
        this.drawString(text, x - 0.5, y, ColorUtils.getColor(0, 0, 0, ColorUtils.getAlphaFromColor(color)), false);
        this.drawString(text, x, y - 0.5, ColorUtils.getColor(0, 0, 0, ColorUtils.getAlphaFromColor(color)), false);
        this.drawString(text, x, y, color, false);
    }

    public float drawCenteredString(String text, double x, double y, int color) {
        return this.drawString(text, x - (double)(this.getStringWidth(text) / 2), y, color);
    }

    public void drawTotalCenteredStringWithShadow(String text, double x, double y, int color) {
        this.drawStringWithShadow(text, x - (double)((float)this.getStringWidth(text) / 2.0F), y - (double)((float)this.getStringHeight(text) / 2.0F), color);
    }

    public float drawCenteredStringWithShadow(String text, double x, double y, int color) {
        return this.drawStringWithShadow(text, x - (double)((float)this.getStringWidth(text) / 2.0F), y, color);
    }

    public List<String> listFormattedStringToWidth(String str, int wrapWidth) {
        return Arrays.asList(this.wrapFormattedStringToWidth(str, wrapWidth).split("\n"));
    }

    public String trimStringToWidth(String text, int width) {
        return this.trimStringToWidth(text, width, false);
    }

    public float drawString(String text, double x, double y, int color, boolean shadow) {
        x--;
        if (text != null && !text.isEmpty()) {
            if (color == 553648127) {
                color = 16777215;
            }

            if ((color & -67108864) == 0) {
                color |= -16777216;
            }

            if (shadow) {
                color = (color & 16579836) >> 2 | color & new Color(20, 20, 20, 200).getRGB();
            }

            CFont.CharData[] currentData = this.charData;
            int alpha = color >> 24 & 0xFF;
            x *= 2.0;
            y = (y - 3.0) * 2.0;
            GlStateManager.scale(0.5, 0.5, 0.5);
            GlStateManager.enableBlend();
            int size = text.length();
            GlStateManager.enableTexture2D();
            GlStateManager.bindTexture(this.tex.getGlTextureId());
            int charColor = color;

            for (int i = 0; i < size; i++) {
                char character = text.charAt(i);
                if (String.valueOf(character).equals("§")) {
                    int colorIndex = 21;

                    try {
                        colorIndex = "0123456789abcdefklmnor".indexOf(text.charAt(i + 1));
                    } catch (Exception var16) {
                    }

                    if (colorIndex < 16) {
                        if (colorIndex < 0) {
                            colorIndex = 15;
                        }

                        if (shadow) {
                            colorIndex += 16;
                        }

                        int colorcode = this.colorCode[colorIndex];
                        charColor = ColorUtils.swapAlpha(colorcode, (float)alpha);
                    } else if (colorIndex == 21 || colorIndex == 17 || colorIndex == 18 || colorIndex == 19 || colorIndex == 20) {
                        charColor = ColorUtils.swapAlpha(color, (float)alpha);
                    }

                    i++;
                } else if (character < currentData.length && currentData != null && currentData[character] != null) {
                    this.bufferBuilder.begin(6, DefaultVertexFormats.POSITION_TEX_COLOR);
                    this.drawChar2(currentData, character, (float)x, (float)y, charColor);
                    this.tessellator.draw();
                    x += (double)(currentData[character].width - 8 + this.charOffset);
                }
            }

            GlStateManager.scale(2.0F, 2.0F, 2.0F);
            return (float)x / 2.0F;
        } else {
            return 0.0F;
        }
    }

    @Override
    public int getStringWidth(String text) {
        if (text != null && !text.isEmpty()) {
            text = NameSecurity.replacedIfActive(text);
            int width = 0;
            CFont.CharData[] currentData = this.charData;
            boolean bold = false;
            boolean italic = false;
            int size = text.length();

            for (int i = 0; i < size; i++) {
                char character = text.charAt(i);
                if (character == 167 && i < size) {
                    int colorIndex = "0123456789abcdefklmnorабвгдеёжзийклмнопрстуфхцчшщъыьэюя".indexOf(character);
                    if (colorIndex < 16) {
                        bold = false;
                        italic = false;
                    } else if (colorIndex == 17) {
                        bold = true;
                        if (italic) {
                            currentData = this.boldItalicChars;
                        } else {
                            currentData = this.boldChars;
                        }
                    } else if (colorIndex == 20) {
                        italic = true;
                        if (bold) {
                            currentData = this.boldItalicChars;
                        } else {
                            currentData = this.italicChars;
                        }
                    } else if (colorIndex == 21) {
                        bold = false;
                        italic = false;
                        currentData = this.charData;
                    }

                    i++;
                } else if (character < currentData.length && character >= 0 && currentData[character] != null) {
                    width += currentData[character].width - 8 + this.charOffset;
                }
            }

            return width / 2;
        } else {
            return 0;
        }
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        this.setupBoldItalicIDs();
    }

    @Override
    public void setAntiAlias(boolean antiAlias) {
        super.setAntiAlias(antiAlias);
        this.setupBoldItalicIDs();
    }

    @Override
    public void setFractionalMetrics(boolean fractionalMetrics) {
        super.setFractionalMetrics(fractionalMetrics);
        this.setupBoldItalicIDs();
    }

    private void setupBoldItalicIDs() {
        this.texBold = this.setupTexture(this.font.deriveFont(1), this.antiAlias, this.fractionalMetrics, this.boldChars);
        this.texItalic = this.setupTexture(this.font.deriveFont(2), this.antiAlias, this.fractionalMetrics, this.italicChars);
    }

    private void drawLine(double x, double y, double x1, double y1, float width) {
        GL11.glDisable(3553);
        GL11.glLineWidth(width);
        GL11.glBegin(1);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x1, y1);
        GL11.glEnd();
        GL11.glEnable(3553);
    }

    public List<String> wrapWords(String text, double width) {
        List finalWords = new ArrayList();
        if ((double)this.getStringWidth(text) > width) {
            String[] words = text.split(" ");
            String currentWord = "";
            char lastColorCode = '\uffff';
            String[] arrayOfString1 = words;
            int j = words.length;

            for (int i = 0; i < j; i++) {
                String word = arrayOfString1[i];

                for (int ii = 0; i < word.toCharArray().length; i++) {
                    char c = word.toCharArray()[i];
                    if (c == 167 && i < word.toCharArray().length - 1) {
                        lastColorCode = word.toCharArray()[i + 1];
                    }
                }

                if ((double)this.getStringWidth(currentWord + word + " ") < width) {
                    currentWord = currentWord + word + " ";
                } else {
                    finalWords.add(currentWord);
                    currentWord = 167 + lastColorCode + word + " ";
                }
            }

            if (currentWord.length() > 0) {
                if ((double)this.getStringWidth(currentWord) < width) {
                    finalWords.add(167 + lastColorCode + currentWord + " ");
                    currentWord = "";
                } else {
                    for (String s : this.formatString(currentWord, width)) {
                        finalWords.add(s);
                    }
                }
            }
        } else {
            finalWords.add(text);
        }

        return finalWords;
    }

    public List<String> formatString(String string, double width) {
        List finalWords = new ArrayList();
        String currentWord = "";
        char lastColorCode = '\uffff';
        char[] chars = string.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == 167 && i < chars.length - 1) {
                lastColorCode = chars[i + 1];
            }

            if ((double)this.getStringWidth(currentWord + c) < width) {
                currentWord = currentWord + c;
            } else {
                finalWords.add(currentWord);
                currentWord = "" + (167 + lastColorCode) + c;
            }
        }

        if (currentWord.length() > 0) {
            finalWords.add(currentWord);
        }

        return finalWords;
    }

    String wrapFormattedStringToWidth(String str, int wrapWidth) {
        if (str.length() <= 1) {
            return str;
        } else {
            int i = this.sizeStringToWidth(str, wrapWidth);
            if (str.length() <= i) {
                return str;
            } else {
                String s = str.substring(0, i);
                char c0 = str.charAt(i);
                boolean flag = c0 == ' ' || c0 == '\n';
                String s1 = getFormatFromString(s) + str.substring(i + (flag ? 1 : 0));
                return s + "\n" + this.wrapFormattedStringToWidth(s1, wrapWidth);
            }
        }
    }

    public static String getFormatFromString(String text) {
        String s = "";
        int i = -1;
        int j = text.length();

        while ((i = text.indexOf(167, i + 1)) != -1) {
            if (i < j - 1) {
                char c0 = text.charAt(i + 1);
                if (isFormatColor(c0)) {
                    s = "§" + c0;
                } else if (isFormatSpecial(c0)) {
                    s = s + "§" + c0;
                }
            }
        }

        return s;
    }

    private int sizeStringToWidth(String str, int wrapWidth) {
        str = NameSecurity.replacedIfActive(str);
        int i = str.length();
        float f = 0.0F;
        int j = 0;
        int k = -1;

        for (boolean flag = false; j < i; j++) {
            char c0 = str.charAt(j);
            switch (c0) {
                case '\n':
                    j--;
                    break;
                case ' ':
                    k = j;
                default:
                    f += this.getCharWidthFloat(c0);
                    if (flag) {
                        f++;
                    }
                    break;
                case '§':
                    if (j < i - 1) {
                        char c1 = str.charAt(++j);
                        if (c1 == 'l' || c1 == 'L') {
                            flag = true;
                        } else if (c1 == 'r' || c1 == 'R' || isFormatColor(c1)) {
                            flag = false;
                        }
                    }
            }

            if (c0 == '\n') {
                k = ++j;
                break;
            }

            if (Math.round(f) > wrapWidth) {
                break;
            }
        }

        return j != i && k != -1 && k < j ? k : j;
    }

    private float getCharWidthFloat(char p_getCharWidthFloat_1_) {
        if (p_getCharWidthFloat_1_ == 167) {
            return -1.0F;
        } else if (p_getCharWidthFloat_1_ != ' ' && p_getCharWidthFloat_1_ != 160) {
            int i = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000"
                    .indexOf(p_getCharWidthFloat_1_);
            if (p_getCharWidthFloat_1_ > 0 && i != -1) {
                return this.charWidthFloat[i];
            } else if (this.glyphWidth[p_getCharWidthFloat_1_] != 0) {
                int j = this.glyphWidth[p_getCharWidthFloat_1_] & 255;
                int k = j >>> 4;
                int l = j & 15;
                l++;
                return (float)((l - k) / 2 + 1);
            } else {
                return 0.0F;
            }
        } else {
            return this.charWidthFloat[32];
        }
    }

    public String trimStringToWidth(String text, int width, boolean reverse) {
        StringBuilder stringbuilder = new StringBuilder();
        float f = 0.0F;
        int i = reverse ? text.length() - 1 : 0;
        int j = reverse ? -1 : 1;
        boolean flag = false;
        boolean flag1 = false;

        for (int k = i; k >= 0 && k < text.length() && f < (float)width; k += j) {
            char c0 = text.charAt(k);
            float f1 = this.getCharWidthFloat(c0);
            if (flag) {
                flag = false;
                if (c0 == 'l' || c0 == 'L') {
                    flag1 = true;
                } else if (c0 == 'r' || c0 == 'R') {
                    flag1 = false;
                }
            } else if (f1 < 0.0F) {
                flag = true;
            } else {
                f += f1;
                if (flag1) {
                    f++;
                }
            }

            if (f > (float)width) {
                break;
            }

            if (reverse) {
                stringbuilder.insert(0, c0);
            } else {
                stringbuilder.append(c0);
            }
        }

        return stringbuilder.toString();
    }

    private static boolean isFormatSpecial(char formatChar) {
        return formatChar >= 'k' && formatChar <= 'o' || formatChar >= 'K' && formatChar <= 'O' || formatChar == 'r' || formatChar == 'R';
    }

    private static boolean isFormatColor(char colorChar) {
        return colorChar >= '0' && colorChar <= '9' || colorChar >= 'a' && colorChar <= 'f' || colorChar >= 'A' && colorChar <= 'F';
    }

    private void setupMinecraftColorcodes() {
        for (int index = 0; index < 32; index++) {
            int noClue = (index >> 3 & 1) * 85;
            int red = (index >> 2 & 1) * 170 + noClue;
            int green = (index >> 1 & 1) * 170 + noClue;
            int blue = (index >> 0 & 1) * 170 + noClue;
            if (index == 6) {
                red += 85;
            }

            if (index >= 16) {
                red /= 4;
                green /= 4;
                blue /= 4;
            }

            this.colorCode[index] = (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
        }
    }
}
