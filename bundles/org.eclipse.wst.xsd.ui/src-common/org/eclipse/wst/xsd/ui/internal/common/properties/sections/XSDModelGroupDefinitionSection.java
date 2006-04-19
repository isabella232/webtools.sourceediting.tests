/*******************************************************************************
 * Copyright (c) 2001, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.xsd.ui.internal.common.properties.sections;

import java.util.ArrayList;
import java.util.List;

import org.apache.xerces.util.XMLChar;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.wst.xsd.ui.internal.common.commands.UpdateNameCommand;
import org.eclipse.wst.xsd.ui.internal.common.util.Messages;
import org.eclipse.wst.xsd.ui.internal.util.TypesHelper;
import org.eclipse.xsd.XSDModelGroupDefinition;
import org.eclipse.xsd.XSDNamedComponent;
import org.eclipse.xsd.util.XSDConstants;
import org.w3c.dom.Element;

public class XSDModelGroupDefinitionSection extends AbstractSection
{
  protected Text nameText;
  protected CCombo componentNameCombo;
  boolean isReference;

  public XSDModelGroupDefinitionSection()
  {
    super();
  }

  protected void createContents(Composite parent)
  {
    composite = getWidgetFactory().createFlatFormComposite(parent);

    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    composite.setLayout(gridLayout);

    if (isReference)
    {
      // ------------------------------------------------------------------
      // Ref Label
      // ------------------------------------------------------------------
      GridData data = new GridData();
      data.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
      data.grabExcessHorizontalSpace = false;
      CLabel refLabel = getWidgetFactory().createCLabel(composite, XSDConstants.REF_ATTRIBUTE + ":"); //$NON-NLS-1$
      refLabel.setLayoutData(data);

      // ------------------------------------------------------------------
      // Ref Combo
      // ------------------------------------------------------------------

      data = new GridData();
      data.grabExcessHorizontalSpace = true;
      data.horizontalAlignment = GridData.FILL;

      componentNameCombo = getWidgetFactory().createCCombo(composite, SWT.FLAT);
      componentNameCombo.addSelectionListener(this);
      componentNameCombo.setLayoutData(data);
    }
    else
    {
      // ------------------------------------------------------------------
      // NameLabel
      // ------------------------------------------------------------------
      GridData data = new GridData();
      data.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
      data.grabExcessHorizontalSpace = false;
      CLabel nameLabel = getWidgetFactory().createCLabel(composite, Messages._UI_LABEL_NAME);
      nameLabel.setLayoutData(data);

      // ------------------------------------------------------------------
      // NameText
      // ------------------------------------------------------------------
      data = new GridData();
      data.grabExcessHorizontalSpace = true;
      data.horizontalAlignment = GridData.FILL;
      nameText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
      nameText.setLayoutData(data);
      nameText.addListener(SWT.Modify, this);
    }
  }

  public void refresh()
  {
    super.refresh();

    if (isReadOnly)
    {
      composite.setEnabled(false);
    }
    else
    {
      composite.setEnabled(true);
    }

    setListenerEnabled(false);

    XSDNamedComponent namedComponent = (XSDNamedComponent) input;

    if (isReference)
    {
      Element element = namedComponent.getElement();
      if (element != null)
      {
        String attrValue = element.getAttribute(XSDConstants.REF_ATTRIBUTE);
        if (attrValue == null)
        {
          attrValue = ""; //$NON-NLS-1$
        }
        componentNameCombo.setText(attrValue);
      }
    }
    else
    {
      // refresh name
      nameText.setText(""); //$NON-NLS-1$

      String name = namedComponent.getName();
      if (name != null)
      {
        nameText.setText(name);
      }
    }

    setListenerEnabled(true);
  }

  public void setInput(IWorkbenchPart part, ISelection selection)
  {
    super.setInput(part, selection);
    init();
    relayout();
    
    if (isReference)
    {
      TypesHelper helper = new TypesHelper(xsdSchema);
      List items = new ArrayList();
      items = helper.getModelGroups();
      items.add(0, ""); //$NON-NLS-1$
      componentNameCombo.setItems((String [])items.toArray(new String[0]));
    }
  }

  protected void init()
  {
    if (input instanceof XSDModelGroupDefinition)
    {
      XSDModelGroupDefinition group = (XSDModelGroupDefinition) input;
      isReference = group.isModelGroupDefinitionReference();
    }
  }

  protected void relayout()
  {
    Composite parentComposite = composite.getParent();
    parentComposite.getParent().setRedraw(false);

    if (parentComposite != null && !parentComposite.isDisposed())
    {
      Control[] children = parentComposite.getChildren();
      for (int i = 0; i < children.length; i++)
      {
        children[i].dispose();
      }
    }

    // Now initialize the new handler
    createContents(parentComposite);
    parentComposite.getParent().layout(true, true);

    // Now turn painting back on
    parentComposite.getParent().setRedraw(true);
    refresh();
  }

  public void doHandleEvent(Event event)
  {
    super.doHandleEvent(event);
    if (event.widget == nameText)
    {
      String newValue = nameText.getText().trim();
      if (input instanceof XSDNamedComponent)
      {
        XSDNamedComponent namedComponent = (XSDNamedComponent) input;
        if (!validateSection())
          return;

        Command command = null;

        // Make sure an actual name change has taken place
        String oldName = namedComponent.getName();
        if (!newValue.equals(oldName))
        {
          command = new UpdateNameCommand(Messages._UI_ACTION_RENAME, namedComponent, newValue);
        }

        if (command != null && getCommandStack() != null)
        {
          getCommandStack().execute(command);
        }
        // doReferentialIntegrityCheck(namedComponent, newValue);
      }
    }
  }

  protected boolean validateSection()
  {
    if (nameText == null || nameText.isDisposed())
      return true;

    setErrorMessage(null);

    String name = nameText.getText().trim();

    // validate against NCName
    if (name.length() < 1 || !XMLChar.isValidNCName(name))
    {
      setErrorMessage(Messages._UI_ERROR_INVALID_NAME);
      return false;
    }

    return true;
  }
  
  public void widgetSelected(SelectionEvent e)
  {
    if (e.widget == componentNameCombo)
    {
      String newValue = componentNameCombo.getText();
      if (input instanceof XSDNamedComponent)
      {
        XSDNamedComponent namedComponent = (XSDNamedComponent)input;
        Element element = namedComponent.getElement();

        if (namedComponent instanceof XSDModelGroupDefinition)
        {
          element.setAttribute(XSDConstants.REF_ATTRIBUTE, newValue);
        }
      }
    }
  }
}
