package fu.hao.netsemantic.test.droidbench;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ Button1Test.class, Button2Test.class, Button3Test.class,
		Button4Test.class, Button5Test.class, Location1Test.class,
		Location2Test.class, Location3Test.class, MethodOverride1.class,
		MultiHandlersTest.class, BenchIACTests.class, BenchAliasing.class,
		BenchICCTests.class, BenchCallbacks.class, BenchReflectionTests.class,
		BenchJavaTests.class, BenchLifecycleTests.class, BenchImplicitTests.class,
		BenchThreadingTests.class, BenchUnknownTests.class,
		Location1Test.class, Location2Test.class, Location3Test.class,
		BenchAndroidSpecific.class, BenchArraysAndLists.class})
public class AllTests {

}
