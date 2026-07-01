package com.raf.fieldops

import com.raf.fieldops.data.model.JobStatusFallbackTest
import com.raf.fieldops.data.model.PriorityFallbackTest
import com.raf.fieldops.ui.adaptive.AdaptiveLayoutTest
import com.raf.fieldops.ui.admin.AdminBadgeTest
import com.raf.fieldops.ui.admin.AdminDashboardScreenTest
import com.raf.fieldops.ui.admin.AdminPendingScreenTest
import com.raf.fieldops.ui.auth.AwaitingApprovalScreenTest
import com.raf.fieldops.ui.auth.EmailConfirmationScreenTest
import com.raf.fieldops.ui.auth.LoginScreenTest
import com.raf.fieldops.ui.auth.RegisterScreenTest
import com.raf.fieldops.ui.auth.RegisterUiStateInstrumentedTest
import com.raf.fieldops.ui.components.ConfirmDialogTest
import com.raf.fieldops.ui.components.CustomButtonTest
import com.raf.fieldops.ui.components.CustomDropDownMenuTest
import com.raf.fieldops.ui.components.CustomTextFieldTest
import com.raf.fieldops.ui.components.DateTimePickerFieldTest
import com.raf.fieldops.ui.components.JobCardTest
import com.raf.fieldops.ui.components.OfflineBannerTest
import com.raf.fieldops.ui.components.PriorityBadgeTest
import com.raf.fieldops.ui.components.StatusChipTest
import com.raf.fieldops.ui.dispatcher.alljobs.DismissedFilterTest
import com.raf.fieldops.ui.dispatcher.createjob.CreateJobScreenTest
import com.raf.fieldops.ui.engineer.home.EngineerHomeScreenTest
import com.raf.fieldops.ui.engineer.home.ExpiredJobDismissTest
import com.raf.fieldops.ui.engineer.upcoming.UpcomingRangeFilterUITest
import com.raf.fieldops.ui.engineer.upcoming.UpcomingRangeNavigationTest
import com.raf.fieldops.ui.nav.AdminNavigationFlowTest
import com.raf.fieldops.ui.nav.DispatcherDrawerNavigationTest
import com.raf.fieldops.ui.nav.DispatcherNavigationFlowTest
import com.raf.fieldops.ui.nav.EngineerTabNavigationTest
import com.raf.fieldops.ui.nav.NavigationFlowTest
import com.raf.fieldops.ui.nav.RegisterNavigationFlowTest
import com.raf.fieldops.ui.profile.ChangePasswordDialogTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(

    JobStatusFallbackTest::class,
    PriorityFallbackTest::class,
    RegisterUiStateInstrumentedTest::class,

    CustomButtonTest::class,
    CustomTextFieldTest::class,
    CustomDropDownMenuTest::class,
    DateTimePickerFieldTest::class,
    StatusChipTest::class,
    JobCardTest::class,
    ConfirmDialogTest::class,
    OfflineBannerTest::class,
    PriorityBadgeTest::class,
    ChangePasswordDialogTest::class,
    EngineerHomeScreenTest::class,
    AdminBadgeTest::class,
    AwaitingApprovalScreenTest::class,
    AdaptiveLayoutTest::class,

    LoginScreenTest::class,
    RegisterScreenTest::class,
    EmailConfirmationScreenTest::class,
    CreateJobScreenTest::class,
    AdminDashboardScreenTest::class,
    AdminPendingScreenTest::class,
    UpcomingRangeFilterUITest::class,

    NavigationFlowTest::class,
    DispatcherNavigationFlowTest::class,
    DispatcherDrawerNavigationTest::class,
    EngineerTabNavigationTest::class,
    RegisterNavigationFlowTest::class,
    ExpiredJobDismissTest::class,
    DismissedFilterTest::class,
    AdminNavigationFlowTest::class,
    UpcomingRangeNavigationTest::class
)
class FieldOpsTestSuite
