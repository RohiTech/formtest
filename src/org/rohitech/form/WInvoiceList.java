package org.rohitech.form;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;

import org.adempiere.webui.component.Grid;

import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.ListModelTable;
import org.adempiere.webui.component.ListboxFactory;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Rows;
import org.adempiere.webui.component.WListbox;
import org.adempiere.webui.editor.WDateEditor;
import org.adempiere.webui.component.Button;

import org.adempiere.webui.panel.ADForm;
import org.adempiere.webui.panel.CustomForm;
import org.adempiere.webui.panel.IFormController;
import org.compiere.minigrid.IMiniTable;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Util;

import org.adempiere.webui.event.WTableModelEvent;
import org.adempiere.webui.event.WTableModelListener;

import org.adempiere.webui.event.ValueChangeListener;
import org.adempiere.webui.event.ValueChangeEvent;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Center;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.North;

public class WInvoiceList extends ADForm 
implements IFormController, EventListener<Event>, WTableModelListener, ValueChangeListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8991114633001233276L;
	// Forma o ventana personalizada consiste en listar facturas
	
	InvoiceList inv = new InvoiceList();
	
	// Contenedores
	
	private CustomForm form = new CustomForm();
	private Borderlayout mainLayout = new Borderlayout();
	
	private Panel parameterPanel = new Panel();
	private Grid parameterLayout = new Grid();
	
	private Panel centerPanel = new Panel();
	private Borderlayout centerLayout = new Borderlayout();
	
	// Componentes (Parametros)
	
	private WListbox invoiceTable = ListboxFactory.newDataTable();
	
	private Label lblFacturaInicio = new Label();
	private Label lblFacturaFin = new Label();
	
	private WDateEditor dtFacturaInicio = new WDateEditor();
	private WDateEditor dtFacturaFin = new WDateEditor();
	
	private Button searchButton = new Button();
	private Button processButton = new Button();
	
	@Override
	protected void initForm()
	{
		// TODO Auto-generated method stub
		dyInit();
		zkInit();
	}
	
	public void processData(IMiniTable tblInvoice)
	{
		// Obtener el total de las filas de la tabla
		int pRows = tblInvoice.getRowCount();
		
		ArrayList<Integer> invoiceList = new ArrayList<Integer>(pRows);
		ArrayList<BigDecimal> amountList = new ArrayList<BigDecimal>(pRows);
		
		BigDecimal totalAmt = Env.ZERO;
		
		// Recorrer cada fila de la tabla
		for(int i = 0; i < pRows; i++)
		{
			// Si la fila es seleccionada
			if(((Boolean) tblInvoice.getValueAt(i, 0)).booleanValue())
			{
				KeyNamePair pp = (KeyNamePair) tblInvoice.getValueAt(i, 1); // Value
				
				// Variables de la factura
				int C_Invoice_ID = pp.getKey();
				invoiceList.add(new Integer(C_Invoice_ID));
				
				BigDecimal InvoiceAmt = (BigDecimal) tblInvoice.getValueAt(i, 5); // Monto de la Factura
				amountList.add(InvoiceAmt);
				
				totalAmt = totalAmt.add(InvoiceAmt);
			}
		}
		
		System.out.println("Numero de Facturas = " + invoiceList.size());
		System.out.println("Monto Total General = " + totalAmt);
	}
	
	public void onEvent(Event e)
	{
		if(e.getTarget().equals(searchButton))
		{
			this.loadInvoices();
		}
		else if(e.getTarget().equals(processButton))
		{
			processData(invoiceTable);
		}
	}
	
	// Este metodo consiste en la carga de datos y creacion del modelo de la tabla
	public void loadInvoices()
	{
		Vector<Vector<Object>> data = inv.getInvoiceList();
		Vector<String> columnNames = inv.getInvoiceListColumnNames();
		
		invoiceTable.clear();
		
		// Remover previamente los escuchadores o listeners
		invoiceTable.getModel().removeTableModelListener(this);
		
		// Establecer el Modelo
		ListModelTable modelP = new ListModelTable(data);
		modelP.addTableModelListener(this);
		invoiceTable.setData(modelP, columnNames);
		inv.setInvoiceListColumnClass(invoiceTable);
	}
	
	public void zkInit()
	{
		form.appendChild(mainLayout);
		mainLayout.setWidth("99%");
		mainLayout.setHeight("100%");
		
		North north = new North();
		north.setStyle("border: none");
		
		mainLayout.appendChild(north);
		north.appendChild(parameterPanel);
		
		Rows rows = null;
		Row row = null;
		
		parameterLayout.setWidth("100%");
		rows = parameterLayout.newRows();
		
		row = rows.newRow();
		Hbox box = new Hbox();
		
		lblFacturaInicio.setHflex("true");
		dtFacturaInicio.getComponent().setHflex("true");
		box.appendChild(lblFacturaInicio.rightAlign());
		box.appendChild(dtFacturaInicio.getComponent());
		
		lblFacturaFin.setHflex("true");
		dtFacturaFin.getComponent().setHflex("true");
		box.appendChild(lblFacturaFin.rightAlign());
		box.appendChild(dtFacturaFin.getComponent());
		
		row.appendCellChild(box);
		
		row = rows.newRow();
		row = rows.newRow();
		
		Hbox box2 = new Hbox();
		
		searchButton.setHflex("true");
		box2.appendChild(searchButton);
		
		processButton.setHflex("true");
		box2.appendChild(processButton);
		
		row.appendCellChild(box2);
		
		parameterPanel.appendChild(parameterLayout);
		
		/****************************************/
		
		centerPanel.setWidth("100%");
		centerPanel.setHeight("100%");
		
		centerLayout.setWidth("100%");
		centerLayout.setHeight("100%");
		centerLayout.setStyle("border: none");
		
		/****************************************/
		
		Center center = new Center();
		center.setStyle("border: none");
		
		mainLayout.appendChild(center);
		center.appendChild(centerPanel);
		
		centerPanel.appendChild(invoiceTable);
		
		/****************************************/
		
		this.appendChild(form);
	}
	
	public void dyInit()
	{
		// Establecer los valores iniciales de los componentes
		lblFacturaInicio.setText("Inicio");
		lblFacturaFin.setText("Fin");
		
		// Establecer la fecha de entrada al sistema
		Calendar cal = Calendar.getInstance();
		cal.setTime(Env.getContextAsDate(Env.getCtx(), "#Date"));
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		dtFacturaInicio.setValue(new Timestamp(cal.getTimeInMillis()));
		dtFacturaInicio.addValueChangeListener(this);
		
		dtFacturaFin.setValue(new Timestamp(cal.getTimeInMillis()));
		dtFacturaFin.addValueChangeListener(this);
		
		searchButton.setLabel(Util.cleanAmp("Buscar"));
		searchButton.addActionListener(this);
		
		processButton.setLabel(Util.cleanAmp("Procesar"));
		processButton.addActionListener(this);
	}
	
	@Override
	public void valueChange(ValueChangeEvent evt)
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void tableChanged(WTableModelEvent event) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public ADForm getForm() {
		// TODO Auto-generated method stub
		return null;
	}
}
