Current state:

  - The build is still on the hybrid fork path: real AOSP android.view.View and related framework classes are being compiled from frameworks/base, with Nova bridges in vendor/nova/nova-framework/src.
  - Error count reduced from 1858 to 684 (63% reduction).
  - The product target is nova-trunk_staging-eng from vendor/nova/products/AndroidProducts.mk.

  What I changed:

  - Type hierarchy fixes:
      - Created android.view.InputEvent (abstract, implements Parcelable) as base class in Nova src
      - Rewrote android.view.MotionEvent to extend InputEvent with full API surface (obtain variants, PointerCoords, PointerProperties, utility methods)
      - Rewrote android.view.KeyEvent to extend InputEvent with full API surface (all KEYCODE_* constants, DispatcherState inner class, normalizeMetaState)
      - Created android.view.ViewParent interface in Nova src (later deleted—let AOSP version from view filegroup provide it)
      - Rewrote android.view.ViewRootImpl to implement ViewParent (all 50+ interface methods, plus mWindowAttributes, mCurScrollY, getWindowInsets, getInsetsController, isViewDescendantOf, setAccessibilityFocus, ensureTouchMode, getAccessibilityFocusedHost)

  - Graphics/drawable bridge:
      - Removed all drawable classes from GRAPHICS_LITE_SRCS in sync-hybrid-framework.py (Drawable, ColorDrawable, BitmapDrawable, GradientDrawable, LayerDrawable, StateListDrawable) — they need full R.attr/R.styleable resource infra that Nova's lightweight R.java can't provide
      - Created Nova bridge Drawable.java with Callback interface and full abstract API surface
      - Created ColorDrawable.java, GradientDrawable.java bridges with static fields (sWrapNegativeAngleMeasurements)
      - Created android.widget.ScrollBarDrawable bridge

  - Expanded existing Nova bridges:
      - android.graphics.Rect: added Rect(Rect) constructor, offset, offsetTo, inset, contains, intersect, union, scale, setEmpty, centerX/Y, equals/hashCode
      - android.graphics.Canvas: added drawRect(int,int,int,int,Paint), drawRect(Rect,Paint), drawColor(int,PorterDuff.Mode), drawColor(long), quickReject(int,int,int,int)
      - android.view.InputDevice: added all SOURCE_CLASS_* and SOURCE_* constants, DEVICE_ID_INVALID, MotionRange with full API
      - android.view.accessibility.AccessibilityEvent: added TYPE_*, CONTENT_CHANGE_TYPE_* constants, content change setter/getters, scroll/action methods
      - android.view.accessibility.AccessibilityNodeInfo: massive expansion (~200 lines)—all ACTION_*, MOVEMENT_GRANULARITY_*, FLAG_* constants, AccessibilityAction objects, is/enable/clickable/focusable methods, bounds, text selection, child management, CollectionInfo/CollectionItemInfo
      - android.view.Surface: added FRAME_RATE_CATEGORY_NO_PREFERENCE/LOW/NORMAL/HIGH/HIGH_HINT
      - android.view.Choreographer: added CALLBACK_INPUT/ANIMATION/INSETS_ANIMATION/TRAVERSAL/COMMIT constants
      - android.util.Log: added LOG_ID_MAIN/RADIO/EVENTS/SYSTEM/CRASH
      - android.content.res.Resources: expanded with getColor(theme), getDrawable(theme), Configuration, newTheme; expanded Theme with obtainStyledAttributes overloads, resolveAttribute, applyStyle, getChangingConfigurations
      - android.content.res.TypedArray: added obtain(resources,set,attrs), obtain(resources,len,data,indices,count), getChangingConfigurations(), extractThemeAttrs()
      - android.view.Window: expanded Callback interface with all 25 methods (dispatch*, onCreate*, onPrepare*, onMenu*, onWindow*, onSearch*, onAction*, keyboard shortcuts, pointer capture)
      - android.R.java: added attr class (state_*, selectableItemBackground, autofilledHighlight), styleable (StateListDrawable entries), drawable (scroll_indicator_material), array (rounded corner configs)

  - Created new Nova bridges/stubs:
      - com.android.internal.util.GrowingArrayUtils (append/insert for all primitive arrays)
      - com.android.internal.util.XmlUtils (XML parse/write helpers)
      - com.android.internal.util.ArrayUtils (isEmpty, contains, appendElement, newUnpadded*, etc.)
      - com.android.internal.widget.ScrollBarUtils (getThumbLength, getThumbOffset)
      - com.android.modules.utils.TypedXmlPullParser / TypedXmlSerializer interfaces
      - android.util.CloseGuard (wraps guard pattern without dalvik.system dep)
      - android.util.FastMath (sin, cos, tan, PI)
      - android.util.StateSet (state matching helpers)
      - android.app.AppGlobals (getInitialApplication, getPackageManager, getIntCoreSetting)
      - android.hardware.input.InputManagerGlobal (singleton, getInputDevice, listener registration)
      - android.os.ParcelableException
      - android.view.FrameMetricsObserver (constructor with Window, Handler, listener)
      - android.view.InsetsController
      - android.view.PointerIcon (all TYPE_* constants, getSystemIcon, create, load, CREATOR)
      - android.view.InputEventReceiver (abstract with onInputEvent, finishInputEvent)
      - android.graphics.animation.RenderNodeAnimator (isNativeInterpolator static)
      - android.graphics.animation.FallbackLUTInterpolator (createNativeInterpolator static)

  - Sync script changes (vendor/nova/nova-framework/tools/sync-hybrid-framework.py):
      - Removed drawable classes from GRAPHICS_LITE_SRCS
      - Removed XmlUtils from INTERNAL_UTIL_EXTRA_EXCLUDES (then re-added; see below)
      - Added PointerIcon, InputEventReceiver, InputEventSender, BatchedInputEventReceiver to VIEW_AOSP_EXCLUDES
      - Added MemoryIntArray, StrictJarFile to UTIL_EXTRA_EXCLUDES (they import dalvik.system.CloseGuard)
      - Added CloseGuard to UTIL_EXTRA_EXCLUDES (wraps dalvik.system.CloseGuard, can't port)

  - Android.bp changes:
      - Briefly added nova-hybrid-internal-util-sources to the srcs list; removed after discovering it pulls in too many AOSP internal-util files with cascading deps

  Where it is blocked now:

  - make -f vendor/nova/Makefile framework still fails in nova-framework-host with 684 errors.
  - The remaining errors are scattered across many individual missing symbols (methods, fields, constants) on existing bridge classes, plus a few missing package stubs:
      - Top missing: R.array references, mChoreographer field, AggStats class, setTargetAccessibilityFocus, various canvas/surface methods
      - Missing packages: com.android.i18n.timezone, android.platform.test.ravenwood, android.util.apk, com.android.modules.utils, com.android.internal.logging, DesktopModeFlags
  - Each batch of fixes reduces errors by ~50-100 but with diminishing returns.

  The most relevant files:
      - vendor/nova/nova-framework/Android.bp
      - vendor/nova/nova-framework/tools/sync-hybrid-framework.py
      - vendor/nova/nova-framework/bridge_files.txt
      - vendor/nova/nova-framework/src/ (224+ .java files after additions)

  Technical conclusion:

  - The approach is still correct.
  - The current implementation is not finished (684 errors remain).
  - The remaining work is substantial but the error categories are now well-understood and each fix follows the same pattern: add missing methods/constants to a Nova bridge class.
  - The most impactful remaining step would be to batch-add all missing symbols from the top 20 categories to their respective bridge files, which should resolve ~200 more errors.
