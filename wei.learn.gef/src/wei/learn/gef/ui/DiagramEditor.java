package wei.learn.gef.ui;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.CreationToolEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.requests.SimpleFactory;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.DirectEditAction;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.parts.GraphicalEditorWithPalette;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import wei.learn.gef.Application;
import wei.learn.gef.editpart.PartFactory;
import wei.learn.gef.helper.IImageKeys;
import wei.learn.gef.model.ArrowConnectionModel;
import wei.learn.gef.model.ContentsModel;
import wei.learn.gef.model.HelloModel;
import wei.learn.gef.model.LineConnectionModel;

public class DiagramEditor extends GraphicalEditorWithPalette {

	// Editor ID
	public static final String ID = "wei.learn.gef.DiagramEditor";
	// an EditDomain is a "session" of editing which contains things like the
	// CommandStack
	GraphicalViewer viewer;

	public DiagramEditor() {
		setEditDomain(new DefaultEditDomain(this));
	}

	@Override
	protected void configureGraphicalViewer() {
		super.configureGraphicalViewer();
		viewer = getGraphicalViewer();
		// ��������������ScalableRootEditPart
		ScalableRootEditPart rootEditPart = new ScalableRootEditPart();
		viewer.setRootEditPart(rootEditPart);
		ZoomManager manager = rootEditPart.getZoomManager();
		// �Ŵ��������
		double[] zoomLevels = new double[] {
				// ���ű����Ǵ�25����2000��
				0.25, 0.5, 0.75, 1.0, 1.5, 2.0, 2.5, 3.0, 4.0, 5.0, 10.0, 20.0 };
		manager.setZoomLevels(zoomLevels); // ��ӷŴ����
		// ���÷ǰٷֱ�����
		ArrayList<String> zoomContributions = new ArrayList<String>();
		zoomContributions.add(ZoomManager.FIT_ALL);
		zoomContributions.add(ZoomManager.FIT_HEIGHT);
		zoomContributions.add(ZoomManager.FIT_WIDTH);
		manager.setZoomLevelContributions(zoomContributions);
		// ע��Ŵ�Action
		IAction action = new ZoomInAction(manager);
		getActionRegistry().registerAction(action);
		// ע����СAction
		action = new ZoomOutAction(manager);
		getActionRegistry().registerAction(action);

		viewer.setEditPartFactory(new PartFactory());
		// �������̾��keyHander
		KeyHandler keyHandler = new KeyHandler();

		// ��DEL��ʱִ��ɾ��Action
		keyHandler.put(KeyStroke.getPressed(SWT.DEL, 127, 0),
				getActionRegistry().getAction(GEFActionConstants.DELETE));
		// ��F2��ʱִ��ֱ�ӱ༭Action
		keyHandler.put(KeyStroke.getPressed(SWT.F2, 0), getActionRegistry()
				.getAction(GEFActionConstants.DIRECT_EDIT));
		getGraphicalViewer().setKeyHandler(keyHandler);
	}

	@Override
	protected void initializeGraphicalViewer() {
		// set the contents of this editor
		ContentsModel contents = new ContentsModel();
		HelloModel child1 = new HelloModel();
		child1.setConstraint(new Rectangle(0, 0, -1, -1));
		contents.addChild(child1);

		HelloModel child2 = new HelloModel();
		child2.setConstraint(new Rectangle(30, 30, -1, -1));
		contents.addChild(child2);
		HelloModel child3 = new HelloModel();
		child3.setConstraint(new Rectangle(10, 80, 80, 50));
		contents.addChild(child3);
		viewer.setContents(contents);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	@Override
	protected PaletteRoot getPaletteRoot() {
		// 1.0 ����һ��palette��route
		PaletteRoot root = new PaletteRoot();
		// 2.0 ����һ�����������ڷ�ֹ����Tools
		PaletteGroup toolGroup = new PaletteGroup("����");

		// 3.0 ����һ��GEF�ṩ��"selection"���� ������ŵ�toolGroup��
		ToolEntry tool = new SelectionToolEntry();
		toolGroup.add(tool);

		// 3.1 ��(ѡ��)����Ϊȱʡ��ѡ��Ĺ���
		root.setDefaultEntry(tool);

		// 4.0 ����һ��GEF�ṩ�� "Marquee��ѡ"���߲�����ŵ�toolGroup��
		tool = new MarqueeToolEntry();
		toolGroup.add(tool);

		// 5.0 ����һ��Drawer(����)���û�ͼ����,�ó�������Ϊ"��ͼ"
		PaletteDrawer drawer = new PaletteDrawer("��ͼ");

		// ָ��"����HelloModelģ��"��������Ӧ��ͼ��
		ImageDescriptor descriptor = AbstractUIPlugin
				.imageDescriptorFromPlugin(Application.PLUGIN_ID,
						"icons//alt_window16.gif");

		// 6.0 ����"����HelloModelģ��"����
		CreationToolEntry creationToolEntry = new CreationToolEntry(
				"����HelloModel", "����HelloModelģ��", new SimpleFactory(
						HelloModel.class), descriptor, descriptor);
		drawer.add(creationToolEntry);

		// ����
		PaletteDrawer connectionDrawer = new PaletteDrawer("����");
		ImageDescriptor newConnectionDescriptor = AbstractUIPlugin
				.imageDescriptorFromPlugin(Application.PLUGIN_ID,
						IImageKeys.jiantou);
		ConnectionCreationToolEntry connCreationEntry = new ConnectionCreationToolEntry(
				"������", "����������", new SimpleFactory(LineConnectionModel.class),
				newConnectionDescriptor, newConnectionDescriptor);
		connectionDrawer.add(connCreationEntry);

		// ��ͷ����
		PaletteDrawer ArrowConnectionDrawer = new PaletteDrawer("��ͷ����");
		ImageDescriptor newArrowConnectionDescriptor = AbstractUIPlugin
				.imageDescriptorFromPlugin(Application.PLUGIN_ID,
						IImageKeys.model);
		ConnectionCreationToolEntry arrowConnCreationEntry = new ConnectionCreationToolEntry(
				"��ͷ����", "������ͷ����",
				new SimpleFactory(ArrowConnectionModel.class),
				newArrowConnectionDescriptor, newArrowConnectionDescriptor);
		ArrowConnectionDrawer.add(arrowConnCreationEntry);
		//
		// //��۽ڵ�
		// PaletteDrawer mergeLineDrawer = new PaletteDrawer("��۽ڵ�");
		// ImageDescriptor mergeLineDescriptor = AbstractUIPlugin
		// .imageDescriptorFromPlugin(Application.PLUGIN_ID,
		// IImageKeys.mergeLine);
		// CreationToolEntry mergeLineCreationEntry = new CreationToolEntry(
		// "��۽ڵ�", "��۽ڵ�",
		// new SimpleFactory(mergeLineModel.class),
		// mergeLineDescriptor, mergeLineDescriptor);
		// mergeLineDrawer.add(mergeLineCreationEntry);

		// 7.0 ��󽫴��������鹤�߼ӵ�root��
		root.add(toolGroup);
		root.add(drawer);
		root.add(connectionDrawer);
		root.add(ArrowConnectionDrawer);
		// root.add(mergeLineDrawer);
		return root;
	}

	@Override
	protected void createActions() {
		super.createActions();
		ActionRegistry registry = getActionRegistry();

		// ������ע��һ��DirectEditAction
		IAction action = new DirectEditAction((IWorkbenchPart) this);
		registry.registerAction(action);

		// ��һ��action��Ҫ��ѡ��������ʱ,��Ҫע����ID ??
		getSelectionActions().add(action.getId());
	
	}

	public Object getAdapter(Class type) {
		if (type == ZoomManager.class) {
			return ((ScalableRootEditPart) getGraphicalViewer()
					.getRootEditPart()).getZoomManager();
		}
		return super.getAdapter(type);
	}
}
