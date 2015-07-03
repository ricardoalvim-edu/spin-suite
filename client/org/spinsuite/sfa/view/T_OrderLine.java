/*************************************************************************************
 * Product: Spin-Suite (Making your Business Spin)                                   *
 * This program is free software; you can redistribute it and/or modify it           *
 * under the terms version 2 of the GNU General Public License as published          *
 * by the Free Software Foundation. This program is distributed in the hope          *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied        *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                  *
 * See the GNU General Public License for more details.                              *
 * You should have received a copy of the GNU General Public License along           *
 * with this program; if not, write to the Free Software Foundation, Inc.,           *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                            *
 * For the text or an alternative of this public license, you may reach us           *
 * Copyright (C) 2012-2015 E.R.P. Consultores y Asociados, S.A. All Rights Reserved. *
 * Contributor(s): Yamel Senih www.erpcya.com                                        *
 *************************************************************************************/
package org.spinsuite.sfa.view;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.logging.Level;

import org.spinsuite.base.DB;
import org.spinsuite.base.R;
import org.spinsuite.interfaces.I_DynamicTab;
import org.spinsuite.model.MOrder;
import org.spinsuite.model.MOrderLine;
import org.spinsuite.sfa.adapters.OrderLineAdapter;
import org.spinsuite.sfa.util.DisplayOrderLine;
import org.spinsuite.util.DisplayType;
import org.spinsuite.util.Env;
import org.spinsuite.util.LogM;
import org.spinsuite.util.Msg;
import org.spinsuite.util.TabParameter;
import org.spinsuite.view.TV_DynamicActivity;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @author Dixon Martinez, dmartinez@erpcya.com, ERPCyA http://www.erpcya.com 12/6/2015, 0:34:00
 * @contributor Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
 *
 */
public class T_OrderLine extends Fragment implements I_DynamicTab {

	/**	Parameters	*/
	private 	TabParameter	 		tabParam				= null;
	private 	ListView				v_list					= null;
	private 	View 					m_View					= null;
	private 	TextView				tv_TotalLines			= null;
	private 	TextView				tv_GrandTotal			= null;
	private 	TextView				tv_lb_TotalLines		= null;
	private 	TextView				tv_lb_GrandTotal		= null;
	private 	boolean					m_IsLoadOk				= false;
	private 	boolean 				m_IsParentModifying		= false;
	private 	boolean 				m_Processed				= false;
	private		int 					m_C_Order_ID			= 0;
	private		TV_DynamicActivity		m_Callback				= null;
	private static final int 			O_DELETE 				= 1;
	
	/**
	 * *** Constructor ***
	 * @author Dixon Martinez, dmartinez@erpcya.com, ERPCyA http://www.erpcya.com
	 */
	public T_OrderLine() {
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//	Current
		if(m_View != null)
			return m_View;
		
		//	Re-Load
		m_View 				= inflater.inflate(R.layout.t_order_line, container, false);
		v_list 				= (ListView) m_View.findViewById(R.id.lv_OrderLine);
		tv_TotalLines 		= (TextView) m_View.findViewById(R.id.tv_TotalLines);
		tv_GrandTotal 		= (TextView) m_View.findViewById(R.id.tv_GrandTotal);
		tv_lb_TotalLines	= (TextView) m_View.findViewById(R.id.tv_lb_TotalLines);
		tv_lb_GrandTotal	= (TextView) m_View.findViewById(R.id.tv_lb_GrandTotal);
		//	Event
		registerForContextMenu(v_list);
		return m_View;
		
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setHasOptionsMenu(true);
    }
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		m_Callback = (TV_DynamicActivity) activity;
	}
	
	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
	    //if(!Env.isCurrentTab(getActivity(), 
	    	//	tabParam.getActivityNo(), tabParam.getTabNo()))
	    	//return;
        //	
        menu.clear();
        inflater.inflate(R.menu.dynamic_tab, menu);
    	//	do it
        //	Get Items
        MenuItem mi_Search 	= menu.findItem(R.id.action_search);
        MenuItem mi_Edit 	= menu.findItem(R.id.action_edit);
        MenuItem mi_Add	 	= menu.findItem(R.id.action_more);
        MenuItem mi_More 	= menu.findItem(R.id.action_more);
        MenuItem mi_Cancel 	= menu.findItem(R.id.action_cancel);
        MenuItem mi_Save 	= menu.findItem(R.id.action_save);
        //	Hide
        mi_Search.setVisible(false);
        mi_Edit.setVisible(false);
        mi_More.setVisible(false);
        mi_Cancel.setVisible(false);
        mi_Save.setVisible(false);
    	//	Valid is Loaded
    	if(!m_IsLoadOk)
    		return;
    	//	Visible Add
    	mi_Add.setEnabled(
				Env.getTabRecord_ID(getActivity(), tabParam.getActivityNo(), 0)[0] > 0
				&& !m_Processed);
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	int itemId = item.getItemId();
    	switch (itemId) {
		case R.id.action_add:
			if(m_IsParentModifying) {
    			Msg.toastMsg(getActivity(), "@ParentRecordModified@");
    			return false;
    		}
			Bundle bundle = new Bundle();
			bundle.putParcelable("TabParam", tabParam);
			bundle.putInt("C_Order_ID", m_C_Order_ID);
			
			Intent intent = new Intent(getActivity(), V_AddOrderLine.class);
			intent.putExtras(bundle);
			startActivityForResult(intent, 0);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onResume() {
    	super.onResume();
    	//	Get Sales Order Identifier
		m_C_Order_ID = Env.getContextAsInt(getActivity(), tabParam.getActivityNo(), "C_Order_ID");
		//	Load Data
		load(); 
		//	Set Processed
		m_Processed = 
				Env.getContextAsBoolean(getActivity(), tabParam.getActivityNo(), "Processed") 
					|| !Env.getWindowsAccess(getActivity(), tabParam.getSPS_Window_ID());
    }

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.lv_OrderLine
				&& !m_Processed) {
			//	Delete
		    menu.add(Menu.NONE, O_DELETE, 
					Menu.NONE, getString(R.string.Action_Delete));
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
	            .getMenuInfo();
	    //	Options
	    switch (item.getItemId()) {
	    	case O_DELETE:
	    		if(m_IsParentModifying) {
	    			Msg.toastMsg(getActivity(), "@ParentRecordModified@");
	    			return false;
	    		}
	    		actionDelete(info.position);
	    		return true;
		    default:
		        return super.onContextItemSelected(item);
	    }
	}
	
	/**
	 * Action Delete 
	 * @author Dixon Martinez, dmartinez@erpcya.com, ERPCyA http://www.erpcya.com
	 * @param position
	 * @return void
	 */
	private void actionDelete(int position) {
		final DisplayOrderLine item = (DisplayOrderLine) v_list.getAdapter().getItem(position);
		String msg_Acept = this.getResources().getString(R.string.msg_Acept);
		Builder ask = Msg.confirmMsg(getActivity(), getResources().getString(R.string.msg_AskDelete));
		ask.setPositiveButton(msg_Acept, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				//	Delete
				MOrderLine oLine = new MOrderLine(getActivity(), item.getC_OrderLine_ID(), null);
				updateHeader(oLine.getCtx(),oLine, oLine.getLineNetAmt(),null);
				try {
					oLine.deleteEx();
				} catch (Exception e) {
					LogM.log(getActivity(), getClass(), Level.SEVERE, "Delete Ordel Line Error", e);
				}
				//	Re-Query
				load();
			}
		});
		ask.show();
	}
	
	/**
	 * Update Header
	 * @author Dixon Martinez, dmartinez@erpcya.com, ERPCyA http://www.erpcya.com
	 * @param ctx
	 * @param oLine
	 * @param p_TotalLines
	 * @param conn
	 * @return void
	 */
	private void updateHeader(Context ctx, MOrderLine oLine,BigDecimal p_TotalLines, DB conn) {
		MOrder order = new MOrder(ctx, oLine.getC_Order_ID(), conn);
		order.setTotalLines(order.getTotalLines().subtract(p_TotalLines));
		if(order.isTaxIncluded())
			order.setGrandTotal(order.getGrandTotal().subtract(p_TotalLines));
		else {
			String sql = "SELECT COALESCE(SUM(it.TaxAmt),0) "
					+ "FROM C_OrderTax it "
					+ "WHERE it.C_Order_ID = " + order.getC_Order_ID();
			BigDecimal taxAmt = new BigDecimal(DB.getSQLValueString(ctx, sql));
			order.setGrandTotal(order.getGrandTotal().subtract(p_TotalLines.add(taxAmt)));
		}
		//	Save
		try {
			order.saveEx();
		} catch (Exception e) {
			LogM.log(getActivity(), getClass(), Level.SEVERE, "Update Order Error", e);
		}
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		//	
		Bundle bundle = getArguments();
		if(bundle != null)
			tabParam = bundle.getParcelable("TabParam");
		//	Is Not ok Load
		if(tabParam == null)
			return;
		//	Set Processed
		m_Processed = 
				Env.getContextAsBoolean(getActivity(), tabParam.getActivityNo(), "Processed") 
					|| !Env.getWindowsAccess(getActivity(), tabParam.getSPS_Window_ID());
		
		//	Get Sales Order Identifier
		m_C_Order_ID = Env.getContextAsInt(getActivity(), tabParam.getActivityNo(), "C_Order_ID");

		//	Load Data
    	if(!m_IsLoadOk)
    		load();
	}
   
	/**
	 * Load Data
	 * @author Dixon Martinez, dmartinez@erpcya.com, ERPCyA http://www.erpcya.com
	 * @return void
	 */
	private void load() {
		if(Env.getTabRecord_ID(getActivity(), tabParam.getActivityNo(), 0)[0] <= 0)
			return;
		//	Load DB
		DB conn = new DB(getActivity());
		DB.loadConnection(conn, DB.READ_ONLY);
		
		int p_C_Order_ID = Env.getContextAsInt(getActivity(), tabParam.getActivityNo(), "C_Order_ID");
		//
		String sql = "SELECT "
				+ "o.TotalLines, "
				+ "o.GrandTotal, "
				+ "c.CurSymbol, "
				+ "ol.C_OrderLine_ID, "
				+ "pc.Name ProductCategory, "
				+ "p.Value, "
				+ "p.Name ProductName, "
				+ "COALESCE(p.Description, '') Description, "
				+ "u.UOMSymbol, "
				+ "ol.PriceEntered, "
				+ "ol.LineNetAmt, "
				+ "ol.QtyEntered "
				+ "FROM C_Order o "
				+ "INNER JOIN C_OrderLine ol ON (o.C_Order_ID = ol.C_Order_ID) "
				+ "INNER JOIN M_Product p ON (ol.M_Product_ID = p.M_Product_ID) "
				+ "INNER JOIN C_UOM u ON (ol.C_UOM_ID = u.C_UOM_ID) "
				+ "INNER JOIN C_Currency c ON(c.C_Currency_ID = o.C_Currency_ID) "
				+ "LEFT JOIN M_Product_Category pc ON(pc.M_Product_Category_ID = p.M_Product_Category_ID) "
				+ "WHERE o.C_Order_ID = ? "
				+ "ORDER BY ol.Line";
		
		
		LogM.log(getActivity(), getClass(), Level.FINE, "SQL=" + sql);
		conn.compileQuery(sql);
		conn.addInt(p_C_Order_ID);
		//	Get SQL
		Cursor rs = conn.querySQL();
		//	
		BigDecimal m_TotalLines = Env.ZERO;
		BigDecimal m_GrandTotal = Env.ZERO;
		String m_CurSymbol = null;
		//	
		ArrayList<DisplayOrderLine> data = new ArrayList<DisplayOrderLine>();
		int m_LinesNo = 0;
		if(rs != null 
				&& rs.moveToFirst()){
			int index = 0;
			m_TotalLines 	= new BigDecimal(rs.getDouble(index++));
			m_GrandTotal 	= new BigDecimal(rs.getDouble(index++));
			m_CurSymbol		= rs.getString(index++);
			//	
			do {
				index = 3;
				m_LinesNo++;
				//
				data.add(
						new DisplayOrderLine(
								rs.getInt(index++),		//	Order Line
								rs.getString(index++),	//	Product Category
								rs.getString(index++),	//	Product Value
								rs.getString(index++),	//	Product Name 
								rs.getString(index++),	//	Product Description
								rs.getString(index++),	//	UOM Symbol
								new BigDecimal(rs.getDouble(index++)),	//	Price Entered
								new BigDecimal(rs.getDouble(index++)),	//	Line Net Amt
								new BigDecimal(rs.getDouble(index++))	//	Qty Entered
								)
						);
				//	
				index = 0;
			} while(rs.moveToNext());
			//	Set Load Ok
			m_IsLoadOk = true;
		}
		//	Close Connection
		DB.closeConnection(conn);
		//	
		DecimalFormat format = DisplayType.getNumberFormat(getActivity(), DisplayType.AMOUNT);
		//	Set Totals
		tv_TotalLines.setText(format.format(m_TotalLines));
		tv_GrandTotal.setText(format.format(m_GrandTotal));
		//	Add Symbol
		if(m_CurSymbol != null) {
			tv_lb_TotalLines.setText(getString(R.string.TotalLines) + " (" + m_CurSymbol + ")");
			tv_lb_GrandTotal.setText(getString(R.string.GrandTotal) + " (" + m_CurSymbol + ")");
		}
		//	Set Info
		if(m_Callback != null) {
			m_Callback.setTabSufix(" (" + m_LinesNo + ")");
		}
		//	Set Adapter
		OrderLineAdapter p_Adapter = new OrderLineAdapter(getActivity(), data);
		p_Adapter.setDropDownViewResource(R.layout.i_ol_add_product);
		v_list.setAdapter(p_Adapter);
	}
	
	@Override
	public void handleMenu() {

	}

	@Override
	public TabParameter getTabParameter() {
		return tabParam;
	}

	@Override
	public void setTabParameter(TabParameter tabParam) {

	}

	@Override
	public boolean refreshFromChange(boolean reQuery) {
		m_IsLoadOk = false;
		return false;
	}

	@Override
	public boolean save() {
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return false;
	}

	@Override
	public boolean isModifying() {
		return false;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		//	Hide Keyboard
		getActivity().getWindow()
					.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		if (resultCode == Activity.RESULT_OK)
			load();
	}

	@Override
	public void setIsParentModifying(boolean isParentModifying) {
		m_IsParentModifying = isParentModifying;
	}

}