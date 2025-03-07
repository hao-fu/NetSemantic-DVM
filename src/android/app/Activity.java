package android.app;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.opencsv.CSVReader;

import android.content.ContextWrapper;
import android.content.Intent;
import android.location.LocationListener;
import android.myclasses.GenInstance;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import patdroid.core.ClassInfo;
import patdroid.core.MethodInfo;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.dvm.DalvikVM.StackFrame;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Pair;
import fu.hao.trust.utils.Settings;

public class Activity extends ContextWrapper implements LocationListener {

	Map<Integer, View> views;
	private final static String TAG = Activity.class.getSimpleName();
	Intent intent;
	FragmentManager fragmentManager = new FragmentManager(this);;
	private static Map<Integer, ClassInfo> widgetPool;
	private View tmpView;
	
	public Activity(DalvikVM vm, ClassInfo type, Intent intent) {
		super(vm, type);
		init(vm, type, intent);
	}
	
	public Activity(DalvikVM vm, ClassInfo type) {
		super(vm, type);
		init(vm, type, null);
	}
	
	@SuppressWarnings("unchecked")
	public void init(DalvikVM vm, ClassInfo type, Intent intent) {
		Log.msg(TAG, "Finish creating new Activity with type: " + type);
		this.intent = intent;
		views = new HashMap<>();
		memUrl = memUrl + "/" + type.fullName;
		if (Settings.execOnCreate) {
			LinkedList<StackFrame> tmpFrames = new LinkedList<>();
			if (vm.getCurrStackFrame() != null && type.findMethods("onCreate")[0].equals(vm.getCurrStackFrame().getMethod())) {
				vm.getStack().removeLast();
			}
			
			Pair<Object, ClassInfo>[] params = (Pair<Object, ClassInfo>[]) new Pair[2]; 
			params[0] = new Pair<Object, ClassInfo>(this, type);
			params[1] = new Pair<Object, ClassInfo>(null, ClassInfo.findClass("android.os.Bundle")); // To restore the saved state.
			StackFrame frame = vm.newStackFrame(type, type.findMethods("onCreate")[0], params, false);
			frame.setIntent(intent);
			Log.bb(TAG, "Intent " + intent);
			tmpFrames.addFirst(frame);
			
			MethodInfo[] onStarts = type.findMethods("onStart");
			if (onStarts != null && onStarts.length > 0) {
				params = (Pair<Object, ClassInfo>[]) new Pair[1]; 
				params[0] = new Pair<Object, ClassInfo>(this, type);
				frame = vm.newStackFrame(type, onStarts[0], params, false);
				tmpFrames.addFirst(frame);
			}
			vm.runInstrumentedMethods(tmpFrames);
		}
	}
	
	public void setContentView(int view) {

	}
	
    public Intent getIntent() {
    	if (intent == null) {
    		Intent in = Settings.getTriggerIntent();
    		if (in != null) {
    			Log.warn(TAG, "Get trigger intent from Settigns " + in);
    			intent = in;
    		} else {
    			// Default is the main
    			intent = new Intent(Intent.ACTION_MAIN);
    		}
    	}
    	return intent;
    }

	public View findViewById(int id) {
		if (views.containsKey(id)) {
			return views.get(id);
		}
	
		View view = null;
		if (widgetPool.containsKey(id)) {
			view = GenInstance.getView(vm, widgetPool.get(id), id);
			view.callDefaultConstructor();
		} else {
			Log.warn(TAG, "Cannot find the view with id " + id);
			view = GenInstance.getView(vm, null, id);
		}
		
		views.put(id, view);
		
		return views.get(id);
	}
	
	public static void xmlViewDefs() {
		try {
			widgetPool = new HashMap<>();
			String csv = Settings.getOutdir() + Settings.getApkName() + "_nid-views.csv";
			File file = new File(csv);
			if (file.exists()) {
				CSVReader reader = new CSVReader(new FileReader(csv));
				for (String[] id_view : reader.readAll()) {
					id_view[0] = id_view[0].replace("x", "");
					int nid = Integer.parseInt(id_view[0], 16); 
					System.out.println(nid);
					ClassInfo clazz = ClassInfo.findClass(id_view[1]);
					if (clazz == null) {
						clazz = ClassInfo.findClass("android.view." + id_view[1]);
						if (clazz == null) {
							clazz = ClassInfo.findClass("android.widget." + id_view[1]);
						}
					}
					System.out.println(id_view[1]);
					System.out.println(clazz);
					widgetPool.put(nid, clazz);
					Log.msg(TAG, "Find xml view with id " + nid);
				}

				reader.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		return "Activity:" + super.toString();
	}
	
    public FragmentManager getFragmentManager() {
        return fragmentManager;
    }
    
    public FragmentManager getSupportFragmentManager() {
        return fragmentManager;
    }

	public Collection<Fragment> getFragments() {
		return getFragmentManager().getFragments();
	}

	
	public Set<DVMObject> getAllUIs() {
		Set<DVMObject> items = new HashSet<>();
		items.addAll(getFragments());
		items.addAll(views.values());
		return items;
	}
	
	public static Map<Integer, ClassInfo> getWidgetPool() {
		return widgetPool;
	}

	public View getTmpView() {
		return tmpView;
	}

	public void setTmpView(View tmpView) {
		this.tmpView = tmpView;
	}
	
	public static void setWidgetPool(Map<Integer, ClassInfo> widgetPool) {
		Activity.widgetPool = widgetPool; 
	}
	
	private Map<Integer, Intent> resultIntents = new HashMap<>();
	
    public final void setResult(int resultCode, Intent data) {
    	resultIntents.put(resultCode, data);
    }
    
    public void finish() {
    }
    
    public void startActivityForResult(Intent intent, int requestCode) {
       MethodInfo[] onActivityResults = type.findMethods("onActivityResult");
       if (onActivityResults != null && onActivityResults.length > 0) {
			@SuppressWarnings("unchecked")
			Pair<Object, ClassInfo>[] params = (Pair<Object, ClassInfo>[]) new Pair[4]; 
			params[0] = new Pair<Object, ClassInfo>(this, type);
			params[1] = new Pair<Object, ClassInfo>(requestCode, ClassInfo.primitiveInt); 
			params[2] = new Pair<Object, ClassInfo>(0, ClassInfo.primitiveInt);
			params[3] = new Pair<Object, ClassInfo>(intent, ClassInfo.findClass("android.content.Intent"));
			StackFrame frame = vm.newStackFrame(type, onActivityResults[0], params, false);
			vm.runInstrumentedMethods(frame);
       } else {
    	   	Log.warn(TAG, "Cannot find the onActivityResult!");
       }
    }
    
    public final void runOnUiThread(Runnable action) {
        throw new RuntimeException("Stub!");
    }
    
    public final void runOnUiThread(DVMObject act) {
    	android.myclasses.Runnable action = (android.myclasses.Runnable)act; 
    	action.run();
    }
    
    public int getRequestedOrientation() {
        throw new RuntimeException("Stub!");
    }
    
    private Window window; 
    public Window getWindow() {
    	if (window == null) {
    		window = new Window(this); 
    	}
    	
    	return window;
    }
    
    private WindowManager windowManager;
    
    public WindowManager getWindowManager() {
    	if (windowManager == null) {
    		windowManager = new WindowManager();
    	}
    	
    	return windowManager;
    }
    
}
