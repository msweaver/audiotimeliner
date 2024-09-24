package util;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class HtmlLineBreakDocumentFilter extends DocumentFilter
{
    public void insertString(DocumentFilter.FilterBypass fb, int offs, String str, AttributeSet a)
                        throws BadLocationException
    {
        super.insertString(fb, offs, str.replaceAll("\n", "\r\n"), a); // works
        //super.insertString(fb, offs, str.replaceAll(" ", "&nbsp;"), a); // works
    }

    public void replace(FilterBypass fb, int offs, int length, String str, AttributeSet a)
        throws BadLocationException
    {
        super.replace(fb, offs, length, str.replaceAll("\n", "\r\n"), a); // works
       // super.replace(fb, offs, length, str.replaceAll(" ", "&nbsp;"), a); // works
    }
}