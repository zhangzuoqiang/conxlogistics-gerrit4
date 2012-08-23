package com.conx.logistics.kernel.ui.common.gwt.client.ui;

/*
 * Copyright 2007 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

// COPIED HERE DUE package privates in GWT
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ui.MenuItem;
import com.vaadin.terminal.gwt.client.ui.VOverlay;

/**
 * A standard menu bar widget. A menu bar can contain any number of menu items,
 * each of which can either fire a {@link com.google.gwt.user.client.Command} or
 * open a cascaded menu bar.
 * 
 * <p>
 * <img class='gallery' src='MenuBar.png'/>
 * </p>
 * 
 * <h3>CSS Style Rules</h3>
 * <ul class='css'>
 * <li>.gwt-MenuBar { the menu bar itself }</li>
 * <li>.gwt-MenuBar .gwt-MenuItem { menu items }</li>
 * <li>
 * .gwt-MenuBar .gwt-MenuItem-selected { selected menu items }</li>
 * </ul>
 * 
 * <p>
 * <h3>Example</h3>
 * {@example com.google.gwt.examples.MenuBarExample}
 * </p>
 * 
 * @deprecated
 */
@Deprecated
public class VConXQuickLaunchMenu extends Widget implements PopupListener {

    private final Element body;
    private final ArrayList<VConXQuickLaunchMenuItem> items = new ArrayList<VConXQuickLaunchMenuItem>();
    private VConXQuickLaunchMenu parentMenu;
    private PopupPanel popup;
    private VConXQuickLaunchMenuItem selectedItem;
    private VConXQuickLaunchMenu shownChildMenu;
    private final boolean vertical;
    private boolean autoOpen;

    /**
     * Creates an empty horizontal menu bar.
     */
    public VConXQuickLaunchMenu() {
        this(false);
    }

    /**
     * Creates an empty menu bar.
     * 
     * @param vertical
     *            <code>true</code> to orient the menu bar vertically
     */
    public VConXQuickLaunchMenu(boolean vertical) {
        super();

        final Element table = DOM.createTable();
        body = DOM.createTBody();
        DOM.appendChild(table, body);

        if (!vertical) {
            final Element tr = DOM.createTR();
            DOM.appendChild(body, tr);
        }

        this.vertical = vertical;

        final Element outer = DOM.createDiv();
        DOM.appendChild(outer, table);
        setElement(outer);

        sinkEvents(Event.ONCLICK | Event.ONMOUSEOVER | Event.ONMOUSEOUT);
        setStyleName("gwt-MenuBar");
    }

    /**
     * Adds a menu item to the bar.
     * 
     * @param item
     *            the item to be added
     */
    public void addItem(VConXQuickLaunchMenuItem item) {
        Element tr;
        if (vertical) {
            tr = DOM.createTR();
            DOM.appendChild(body, tr);
        } else {
            tr = DOM.getChild(body, 0);
        }

        DOM.appendChild(tr, item.getElement());

        item.setParentMenu(this);
        item.setSelectionStyle(false);
        items.add(item);
    }

    /**
     * Adds a menu item to the bar, that will fire the given command when it is
     * selected.
     * 
     * @param text
     *            the item's text
     * @param asHTML
     *            <code>true</code> to treat the specified text as html
     * @param cmd
     *            the command to be fired
     * @return the {@link MenuItem} object created
     */
    public VConXQuickLaunchMenuItem addItem(String text, boolean asHTML, Command cmd) {
        final VConXQuickLaunchMenuItem item = new VConXQuickLaunchMenuItem(text, asHTML, cmd);
        addItem(item);
        return item;
    }

    /**
     * Adds a menu item to the bar, that will open the specified menu when it is
     * selected.
     * 
     * @param text
     *            the item's text
     * @param asHTML
     *            <code>true</code> to treat the specified text as html
     * @param popup
     *            the menu to be cascaded from it
     * @return the {@link MenuItem} object created
     */
    public VConXQuickLaunchMenuItem addItem(String text, boolean asHTML, VConXQuickLaunchMenu popup) {
        final VConXQuickLaunchMenuItem item = new VConXQuickLaunchMenuItem(text, asHTML, popup);
        addItem(item);
        return item;
    }

    /**
     * Adds a menu item to the bar, that will fire the given command when it is
     * selected.
     * 
     * @param text
     *            the item's text
     * @param cmd
     *            the command to be fired
     * @return the {@link MenuItem} object created
     */
    public VConXQuickLaunchMenuItem addItem(String text, Command cmd) {
        final VConXQuickLaunchMenuItem item = new VConXQuickLaunchMenuItem(text, cmd);
        addItem(item);
        return item;
    }

    /**
     * Adds a menu item to the bar, that will open the specified menu when it is
     * selected.
     * 
     * @param text
     *            the item's text
     * @param popup
     *            the menu to be cascaded from it
     * @return the {@link MenuItem} object created
     */
    public VConXQuickLaunchMenuItem addItem(String text, VConXQuickLaunchMenu popup) {
        final VConXQuickLaunchMenuItem item = new VConXQuickLaunchMenuItem(text, popup);
        addItem(item);
        return item;
    }

    /**
     * Removes all menu items from this menu bar.
     */
    public void clearItems() {
        final Element container = getItemContainerElement();
        while (DOM.getChildCount(container) > 0) {
            DOM.removeChild(container, DOM.getChild(container, 0));
        }
        items.clear();
    }

    /**
     * Gets whether this menu bar's child menus will open when the mouse is
     * moved over it.
     * 
     * @return <code>true</code> if child menus will auto-open
     */
    public boolean getAutoOpen() {
        return autoOpen;
    }

    @Override
    public void onBrowserEvent(Event event) {
        super.onBrowserEvent(event);

        final VConXQuickLaunchMenuItem item = findItem(DOM.eventGetTarget(event));
        switch (DOM.eventGetType(event)) {
        case Event.ONCLICK: {
            // Fire an item's command when the user clicks on it.
            if (item != null) {
                doItemAction(item, true);
            }
            break;
        }

        case Event.ONMOUSEOVER: {
            if (item != null) {
                itemOver(item);
            }
            break;
        }

        case Event.ONMOUSEOUT: {
            if (item != null) {
                itemOver(null);
            }
            break;
        }
        }
    }

    public void onPopupClosed(PopupPanel sender, boolean autoClosed) {
        // If the menu popup was auto-closed, close all of its parents as well.
        if (autoClosed) {
            closeAllParents();
        }

        // When the menu popup closes, remember that no item is
        // currently showing a popup menu.
        onHide();
        shownChildMenu = null;
        popup = null;
    }

    /**
     * Removes the specified menu item from the bar.
     * 
     * @param item
     *            the item to be removed
     */
    public void removeItem(MenuItem item) {
        final int idx = items.indexOf(item);
        if (idx == -1) {
            return;
        }

        final Element container = getItemContainerElement();
        DOM.removeChild(container, DOM.getChild(container, idx));
        items.remove(idx);
    }

    /**
     * Sets whether this menu bar's child menus will open when the mouse is
     * moved over it.
     * 
     * @param autoOpen
     *            <code>true</code> to cause child menus to auto-open
     */
    public void setAutoOpen(boolean autoOpen) {
        this.autoOpen = autoOpen;
    }

    /**
     * Returns a list containing the <code>MenuItem</code> objects in the menu
     * bar. If there are no items in the menu bar, then an empty
     * <code>List</code> object will be returned.
     * 
     * @return a list containing the <code>MenuItem</code> objects in the menu
     *         bar
     */
    protected List<VConXQuickLaunchMenuItem> getItems() {
        return items;
    }

    /**
     * Returns the <code>MenuItem</code> that is currently selected
     * (highlighted) by the user. If none of the items in the menu are currently
     * selected, then <code>null</code> will be returned.
     * 
     * @return the <code>MenuItem</code> that is currently selected, or
     *         <code>null</code> if no items are currently selected
     */
    protected VConXQuickLaunchMenuItem getSelectedItem() {
        return selectedItem;
    }

    @Override
    protected void onDetach() {
        // When the menu is detached, make sure to close all of its children.
        if (popup != null) {
            popup.hide();
        }

        super.onDetach();
    }

    /*
     * Closes all parent menu popups.
     */
    void closeAllParents() {
        VConXQuickLaunchMenu curMenu = this;
        while (curMenu != null) {
            curMenu.close();

            if ((curMenu.parentMenu == null) && (curMenu.selectedItem != null)) {
                curMenu.selectedItem.setSelectionStyle(false);
                curMenu.selectedItem = null;
            }

            curMenu = curMenu.parentMenu;
        }
    }

    /*
     * Performs the action associated with the given menu item. If the item has
     * a popup associated with it, the popup will be shown. If it has a command
     * associated with it, and 'fireCommand' is true, then the command will be
     * fired. Popups associated with other items will be hidden.
     * 
     * @param item the item whose popup is to be shown. @param fireCommand
     * <code>true</code> if the item's command should be fired,
     * <code>false</code> otherwise.
     */
    void doItemAction(final VConXQuickLaunchMenuItem item, boolean fireCommand) {
        // If the given item is already showing its menu, we're done.
        if ((shownChildMenu != null) && (item.getSubMenu() == shownChildMenu)) {
            return;
        }

        // If another item is showing its menu, then hide it.
        if (shownChildMenu != null) {
            shownChildMenu.onHide();
            popup.hide();
        }

        // If the item has no popup, optionally fire its command.
        if (item.getSubMenu() == null) {
            if (fireCommand) {
                // Close this menu and all of its parents.
                closeAllParents();

                // Fire the item's command.
                final Command cmd = item.getCommand();
                if (cmd != null) {
                    Scheduler.get().scheduleDeferred(cmd);
                }
            }
            return;
        }

        // Ensure that the item is selected.
        selectItem(item);

        // Create a new popup for this item, and position it next to
        // the item (below if this is a horizontal menu bar, to the
        // right if it's a vertical bar).
        popup = new VOverlay(true) {
            {
                setWidget(item.getSubMenu());
                item.getSubMenu().onShow();
            }

            @Override
            public boolean onEventPreview(Event event) {
                // Hook the popup panel's event preview. We use this to keep it
                // from
                // auto-hiding when the parent menu is clicked.
                switch (DOM.eventGetType(event)) {
                case Event.ONCLICK:
                    // If the event target is part of the parent menu, suppress
                    // the
                    // event altogether.
                    final Element target = DOM.eventGetTarget(event);
                    final Element parentMenuElement = item.getParentMenu()
                            .getElement();
                    if (DOM.isOrHasChild(parentMenuElement, target)) {
                        return false;
                    }
                    break;
                }

                return super.onEventPreview(event);
            }
        };
        popup.addPopupListener(this);

        if (vertical) {
            popup.setPopupPosition(
                    item.getAbsoluteLeft() + item.getOffsetWidth(),
                    item.getAbsoluteTop());
        } else {
            popup.setPopupPosition(item.getAbsoluteLeft(),
                    item.getAbsoluteTop() + item.getOffsetHeight());
        }

        shownChildMenu = item.getSubMenu();
        item.getSubMenu().parentMenu = this;

        // Show the popup, ensuring that the menubar's event preview remains on
        // top
        // of the popup's.
        popup.show();
    }

    void itemOver(VConXQuickLaunchMenuItem item) {
        if (item == null) {
            // Don't clear selection if the currently selected item's menu is
            // showing.
            if ((selectedItem != null)
                    && (shownChildMenu == selectedItem.getSubMenu())) {
                return;
            }
        }

        // Style the item selected when the mouse enters.
        selectItem(item);

        // If child menus are being shown, or this menu is itself
        // a child menu, automatically show an item's child menu
        // when the mouse enters.
        if (item != null) {
            if ((shownChildMenu != null) || (parentMenu != null) || autoOpen) {
                doItemAction(item, false);
            }
        }
    }

    void selectItem(VConXQuickLaunchMenuItem item) {
        if (item == selectedItem) {
            return;
        }

        if (selectedItem != null) {
            selectedItem.setSelectionStyle(false);
        }

        if (item != null) {
            item.setSelectionStyle(true);
        }

        selectedItem = item;
    }

    /**
     * Closes this menu (if it is a popup).
     */
    private void close() {
        if (parentMenu != null) {
            parentMenu.popup.hide();
        }
    }

    private VConXQuickLaunchMenuItem findItem(Element hItem) {
        for (int i = 0; i < items.size(); ++i) {
            final VConXQuickLaunchMenuItem item = items.get(i);
            if (DOM.isOrHasChild(item.getElement(), hItem)) {
                return item;
            }
        }

        return null;
    }

    private Element getItemContainerElement() {
        if (vertical) {
            return body;
        } else {
            return DOM.getChild(body, 0);
        }
    }

    /*
     * This method is called when a menu bar is hidden, so that it can hide any
     * child popups that are currently being shown.
     */
    private void onHide() {
        if (shownChildMenu != null) {
            shownChildMenu.onHide();
            popup.hide();
        }
    }

    /*
     * This method is called when a menu bar is shown.
     */
    private void onShow() {
        // Select the first item when a menu is shown.
        if (items.size() > 0) {
            selectItem(items.get(0));
        }
    }
}
