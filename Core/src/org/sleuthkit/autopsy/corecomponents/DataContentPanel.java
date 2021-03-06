/*
 * Autopsy Forensic Browser
 * 
 * Copyright 2011-2014 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.autopsy.corecomponents;

import java.awt.Cursor;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.sleuthkit.autopsy.core.UserPreferences;
import org.sleuthkit.autopsy.corecomponentinterfaces.DataContent;
import org.sleuthkit.autopsy.corecomponentinterfaces.DataContentViewer;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.datamodel.TskCoreException;

/**
 *
 */
 public class DataContentPanel extends javax.swing.JPanel implements DataContent, ChangeListener {
    
    private static Logger logger = Logger.getLogger(DataContentPanel.class.getName());
    private final List<UpdateWrapper> viewers = new ArrayList<>();;
    private Node currentNode;
    private final boolean isMain;
    private boolean listeningToTabbedPane = false;    
     
    /**
     * Creates new DataContentPanel panel
     * The main data content panel can only be created by the data content top component, 
     * thus this constructor is not public.
     * 
     * Use the createInstance factory method to create an external viewer data content panel.
     * 
     */
    DataContentPanel(boolean isMain) {
        this.isMain = isMain;
        initComponents();
        
        // add all implementors of DataContentViewer and put them in the tabbed pane
        Collection<? extends DataContentViewer> dcvs = Lookup.getDefault().lookupAll(DataContentViewer.class);
        for (DataContentViewer factory : dcvs) {
            DataContentViewer dcv;
            if (isMain) {
                //use the instance from Lookup for the main viewer
                dcv = factory; 
            }
            else {
                dcv = factory.createInstance(); 
            }
            viewers.add(new UpdateWrapper(dcv));
            jTabbedPane1.addTab(dcv.getTitle(), null,
                    dcv.getComponent(), dcv.getToolTip());
        }
        
        // disable the tabs
        int numTabs = jTabbedPane1.getTabCount();
        for (int tab = 0; tab < numTabs; ++tab) {
            jTabbedPane1.setEnabledAt(tab, false);
        }
    }
    
    
    /**
     * Factory method to create an external (not main window) data content panel
     * to be used in an external window
     * 
     * @return a new instance of a data content panel
     */
    public static DataContentPanel createInstance() {
        return new DataContentPanel(false);
    }
    
    public JTabbedPane getTabPanels() {
        return jTabbedPane1;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();

        setMinimumSize(new java.awt.Dimension(5, 5));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void setNode(Node selectedNode) {
        // change the cursor to "waiting cursor" for this operation
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            
            
            String defaultName = NbBundle.getMessage(DataContentTopComponent.class, "CTL_DataContentTopComponent");
            // set the file path
            if (selectedNode == null) {
                setName(defaultName);
            } else {
                Content content = selectedNode.getLookup().lookup(Content.class);
                if (content != null) {
                    //String path = DataConversion.getformattedPath(ContentUtils.getDisplayPath(selectedNode.getLookup().lookup(Content.class)), 0);
                    String path = defaultName;
                    try {
                        path = content.getUniquePath();
                    } catch (TskCoreException ex) {
                        logger.log(Level.SEVERE, "Exception while calling Content.getUniquePath() for {0}", content); //NON-NLS
                    }
                    setName(path);
                } else {
                    setName(defaultName);
                }
            }

            currentNode = selectedNode;

            setupTabs(selectedNode);
        } finally {
            this.setCursor(null);
        }
    }
    
    /**
     * Resets the tabs based on the selected Node. If the selected node is null
     * or not supported, disable that tab as well.
     *
     * @param selectedNode  the selected content Node
     */
    public void setupTabs(Node selectedNode) {
        // Deferring becoming a listener to the tabbed pane until this point
        // eliminates handling a superfluous stateChanged event during construction.
        if (listeningToTabbedPane == false) {
            jTabbedPane1.addChangeListener(this);        
            listeningToTabbedPane = true;
        }
                
        int currTabIndex = jTabbedPane1.getSelectedIndex();
        int totalTabs = jTabbedPane1.getTabCount();
        int maxPreferred = 0;
        int preferredViewerIndex = 0;
        for (int i = 0; i < totalTabs; ++i) {
            UpdateWrapper dcv = viewers.get(i);
            dcv.resetComponent();

            // disable an unsupported tab (ex: picture viewer)
            if ((selectedNode == null) || (dcv.isSupported(selectedNode) == false)) {
                jTabbedPane1.setEnabledAt(i, false);
            } else {
                jTabbedPane1.setEnabledAt(i, true);
                
                // remember the viewer with the highest preference value
                int currentPreferred = dcv.isPreferred(selectedNode);
                if (currentPreferred > maxPreferred) {
                    preferredViewerIndex = i;
                    maxPreferred = currentPreferred;
                }
            }
        }
        
        // let the user decide if we should stay with the current viewer
        int tabIndex = UserPreferences.keepPreferredContentViewer() ? currTabIndex : preferredViewerIndex;
        
        UpdateWrapper dcv = viewers.get(tabIndex);
        // this is really only needed if no tabs were enabled 
        if (jTabbedPane1.isEnabledAt(tabIndex) == false) {
            dcv.resetComponent();
        }
        else {
            dcv.setNode(selectedNode);
        }
        
        // set the tab to the one the user wants, then set that viewer's node.
        jTabbedPane1.setSelectedIndex(tabIndex);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    }

    @Override
    public void stateChanged(ChangeEvent evt) {
        JTabbedPane pane = (JTabbedPane) evt.getSource();

        // Get and set current selected tab
        int currentTab = pane.getSelectedIndex();
        if (currentTab != -1) {
            UpdateWrapper dcv = viewers.get(currentTab);
            if (dcv.isOutdated()) {
                // change the cursor to "waiting cursor" for this operation
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    dcv.setNode(currentNode);
                } finally {
                    this.setCursor(null);
                }
            }
        }
    }

    private static class UpdateWrapper {

        private DataContentViewer wrapped;
        private boolean outdated;

        UpdateWrapper(DataContentViewer wrapped) {
            this.wrapped = wrapped;
            this.outdated = true;
        }

        void setNode(Node selectedNode) {
            this.wrapped.setNode(selectedNode);
            this.outdated = false;
        }

        void resetComponent() {
            this.wrapped.resetComponent();
            this.outdated = true;
        }

        boolean isOutdated() {
            return this.outdated;
        }

        boolean isSupported(Node node) {
            return this.wrapped.isSupported(node);
        }
        
        int isPreferred(Node node) {
            return this.wrapped.isPreferred(node);
        }
    }
    
}
