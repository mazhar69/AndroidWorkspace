/*
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * "The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations under
 * the License.
 *
 * The Original Code is ICEpdf 3.0 open source software code, released
 * May 1st, 2009. The Initial Developer of the Original Code is ICEsoft
 * Technologies Canada, Corp. Portions created by ICEsoft are Copyright (C)
 * 2004-2009 ICEsoft Technologies Canada, Corp. All Rights Reserved.
 *
 * Contributor(s): _____________________.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"
 * License), in which case the provisions of the LGPL License are
 * applicable instead of those above. If you wish to allow use of your
 * version of this file only under the terms of the LGPL License and not to
 * allow others to use your version of this file under the MPL, indicate
 * your decision by deleting the provisions above and replace them with
 * the notice and other provisions required by the LGPL License. If you do
 * not delete the provisions above, a recipient may use your version of
 * this file under either the MPL or the LGPL License."
 *
 */
package org.icepdf.core.pobjects.graphics;

import org.icepdf.core.pobjects.PRectangle;
import org.icepdf.core.pobjects.fonts.Font;
import org.icepdf.core.pobjects.fonts.FontFile;

import android.graphics.Matrix;
import android.graphics.PointF;

/**
 * The text state comprises those graphics state parameters that only affect text.
 * <p/>
 * Tc - Character spacing
 * Tw - Word spacing
 * Th - Horizontal scaling
 * Tl - Leading Tf Text font
 * Tfs - Text font size
 * Tmode - Text rendering mode
 * Trise - Text rise
 * Tk - Text knockout
 *
 * @since 2.0
 */
public class TextState {

    /**
     * Fill text
     */
    public static final int MODE_FILL = 0;

    /**
     * Stroke text
     */
    public static final int MODE_STROKE = 1;
    /**
     * Fill then stroke text.
     */
    public static final int MODE_FILL_STROKE = 2;
    /**
     * Neither fill nor stroke text
     */
    public static final int MODE_INVISIBLE = 3;
    /**
     * Fill text and add to path for clipping.
     */
    public static final int MODE_FILL_ADD = 4;
    /**
     * Stroke text and add to path for clipping
     */
    public static final int MODE_STROKE_ADD = 5;
    /**
     * Fill then stroke,text and add to path for clipping
     */
    public static final int MODE_FILL_STROKE_ADD = 6;

    /**
     * Add text to path for clipping
     */
    public static final int MODE_ADD = 7;

    // type3 font text states for d1 token.
    protected PRectangle type3BBox;
    // type 3 font text state for d0 token.
    protected PointF type3HorizontalDisplacement;

    /**
     * Set the character spacing, Tc, to charSpace, which is a number expressed
     * in unscaled text space units. Character spacing is used by the Tj, TJ,
     * and ' operators. Initial value: 0.
     */
    public float cspace = 0;

    /**
     * Set the word spacing, Tw, to wordSpace, which is a number expressed in
     * unscaled text space units. Word spacing is used by the Tj, TJ, and '
     * operators. Initial value: 0.
     */
    public float wspace = 0;

    /**
     * Set the horizontal scaling, Th, to (scale � 100). scale is a number
     * specifying the percentage of the normal width. Initial value: 100
     * (normal width).
     */
    public float hScalling = 1;

    /**
     * Set the text leading, Tl, to leading, which is a number expressed in
     * unscaled text space units. Text leading is used only by the T*, ',
     * and " operators. Initial value: 0.
     */
    public float leading = 0;

    /**
     * Text Font size
     */
    public float tsize = 0;
    /**
     * Set the text rendering mode, Tmode, to render, which is an integer.
     * Initial value: 0.
     */
    public int rmode = 0;
    /**
     * Set the text rise, Trise, to rise, which is a number expressed in
     * unscaled text space units. Initial value: 0.
     */
    public float trise = 0;

    /**
     * Transformation matrix defined by the Tm tag
     */
    public Matrix tmatrix = new Matrix();
    public Matrix tlmatrix = new Matrix();

    /**
     * Text Font - Associated ICEpdf font object
     */
    public Font font;

    /**
     * Text Font - Associated awt font object for display purposes
     */
    public FontFile currentfont;

    /**
     * Create a new Instance of TextState
     */
    public TextState() {
    }

    /**
     * Creat a new instance of TextState. All text state properites are copied
     * from <code>ts</code>.
     *
     * @param ts text state to
     */
    public TextState(TextState ts) {
        // map properties
        cspace = ts.cspace;
        wspace = ts.wspace;
        hScalling = ts.hScalling;
        leading = ts.leading;
        font = ts.font;
        // create a new clone based on current font, cheap clone
        currentfont = ts.currentfont != null ?
                ts.currentfont.deriveFont(new Matrix()) : null;
        tsize = ts.tsize;
        tmatrix = new Matrix(ts.tmatrix);
        tlmatrix = new Matrix(ts.tlmatrix);
        rmode = ts.rmode;
        trise = ts.trise;
    }

    public PRectangle getType3BBox() {
        return type3BBox;
    }

    public void setType3BBox(PRectangle type3BBox) {
        this.type3BBox = type3BBox;
    }

    public PointF getType3HorizontalDisplacement() {
        return type3HorizontalDisplacement;
    }

    public void setType3HorizontalDisplacement(PointF type3HorizontalDisplacement) {
        this.type3HorizontalDisplacement = type3HorizontalDisplacement;
    }

}



