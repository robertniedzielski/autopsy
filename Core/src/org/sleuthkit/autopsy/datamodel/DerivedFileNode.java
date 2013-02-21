/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sleuthkit.autopsy.datamodel;

import java.util.LinkedHashMap;
import java.util.Map;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.sleuthkit.datamodel.DerivedFile;
import org.sleuthkit.datamodel.LayoutFile;
import org.sleuthkit.datamodel.TskCoreException;

/**
 * A Node for a DerivedFile content object.
 * 
 * TODO should be able to extend FileNode after FileNode extends AbstractFsContentNode<AbstractFile>
 */
public class DerivedFileNode  extends AbstractAbstractFileNode<DerivedFile> {

    public static String nameForLayoutFile(LayoutFile lf) {
        return lf.getName();
    }

    public DerivedFileNode(DerivedFile df) {
        super(df);

        this.setDisplayName(df.getName());
        
         // set name, display name, and icon
        if (df.isDir()) {
            this.setIconBaseWithExtension("org/sleuthkit/autopsy/images/Folder-icon.png");
        } else {
            this.setIconBaseWithExtension(FileNode.getIconForFileType(df));
        }
        
    }

    @Override
    public TYPE getDisplayableItemNodeType() {
        return TYPE.CONTENT;
    }

    @Override
    protected Sheet createSheet() {
        Sheet s = super.createSheet();
        Sheet.Set ss = s.get(Sheet.PROPERTIES);
        if (ss == null) {
            ss = Sheet.createPropertiesSet();
            s.put(ss);
        }

        Map<String, Object> map = new LinkedHashMap<String, Object>();
        fillPropertyMap(map, content);

        ss.put(new NodeProperty("Name", "Name", "no description", getName()));

        final String NO_DESCR = "no description";
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            ss.put(new NodeProperty(entry.getKey(), entry.getKey(), NO_DESCR, entry.getValue()));
        }
        // @@@ add more properties here...

        return s;
    }

    @Override
    public <T> T accept(ContentNodeVisitor<T> v) {
        return v.visit(this);
    }

    @Override
    public <T> T accept(DisplayableItemNodeVisitor<T> v) {
        return v.visit(this);
    }

    @Override
    public boolean isLeafTypeNode() {
        return true; //!this.hasContentChildren();
    }
    
    
    

}