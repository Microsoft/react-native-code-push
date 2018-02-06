package com.microsoft.codepush.common;

import com.microsoft.appcenter.utils.AppCenterLog;
import com.microsoft.codepush.common.datacontracts.CodePushDeploymentStatusReport;
import com.microsoft.codepush.common.datacontracts.CodePushDownloadStatusReport;
import com.microsoft.codepush.common.datacontracts.CodePushLocalPackage;
import com.microsoft.codepush.common.datacontracts.CodePushPackage;
import com.microsoft.codepush.common.datacontracts.CodePushUpdateRequest;
import com.microsoft.codepush.common.datacontracts.CodePushUpdateResponse;
import com.microsoft.codepush.common.utils.FileUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.verification.VerificationModeFactory;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

/**
 * This class tests cases where an error happens and should be logged via {@link AppCenterLog#error(String, String)}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(AppCenterLog.class)
public class LoggingUnitTests {

    private final static String CLIENT_UNIQUE_ID = "YHFfdsfdv65";
    private final static String DEPLOYMENT_KEY = "ABC123";
    private final static String LABEL = "awesome package";
    private final static boolean FAILED_INSTALL = false;
    private final static boolean IS_PENDING = true;
    private final static boolean IS_DEBUG_ONLY = false;
    private final static boolean IS_FIRST_RUN = false;
    private final static String APP_ENTRY_POINT = "/www/index.html";

    private FileUtils mFileUtils;

    @Before
    public void setUp() {
        this.mFileUtils = FileUtils.getInstance();
    }

    @Test
    public void testLogging() throws Exception {
        FileUtils fileUtils = FileUtils.getInstance();
        CodePushPackage codePushPackage = new CodePushPackage();
        CodePushLocalPackage codePushLocalPackage = CodePushLocalPackage.createLocalPackage(FAILED_INSTALL, IS_FIRST_RUN, IS_PENDING, IS_DEBUG_ONLY, APP_ENTRY_POINT, codePushPackage);
        CodePushUpdateRequest codePushUpdateRequest = CodePushUpdateRequest.createUpdateRequest(DEPLOYMENT_KEY, codePushLocalPackage, CLIENT_UNIQUE_ID);
        CodePushDownloadStatusReport codePushDownloadStatusReport = CodePushDownloadStatusReport.createReport(CLIENT_UNIQUE_ID, DEPLOYMENT_KEY, LABEL);
        CodePushDeploymentStatusReport codePushDeploymentStatusReport = new CodePushDeploymentStatusReport();
        CodePushUpdateResponse codePushUpdateResponse = new CodePushUpdateResponse();

         /* Verify errors are logged. */
        mockStatic(AppCenterLog.class);
        codePushUpdateRequest.setDeploymentKey(null);
        codePushUpdateRequest.setAppVersion(null);
        codePushDownloadStatusReport.setLabel(null);
        codePushDownloadStatusReport.setClientUniqueId(null);
        codePushDownloadStatusReport.setDeploymentKey(null);
        codePushDeploymentStatusReport.setAppVersion(null);
        codePushDeploymentStatusReport.setPreviousDeploymentKey(null);
        codePushUpdateResponse.setUpdateInfo(null);
        File testFile = mock(File.class);
        doReturn(null).when(testFile).listFiles();
        doReturn(true).when(testFile).isDirectory();
        fileUtils.deleteFileOrFolderSilently(testFile);
        testFile = mock(File.class);
        doReturn(false).when(testFile).delete();
        fileUtils.deleteFileOrFolderSilently(testFile);
        File newTestFile = mock(File.class);
        doReturn(true).when(newTestFile).isDirectory();
        doReturn(new File[]{testFile}).when(newTestFile).listFiles();
        fileUtils.deleteFileOrFolderSilently(newTestFile);

        /* We expect exactly 12 logged errors. */
        verifyStatic(VerificationModeFactory.times(12));
        AppCenterLog.error(eq(CodePush.LOG_TAG), anyString());
    }

    /**
     * Checks {@link FileUtils#finalizeResources} logs custom error message.
     */
    @Test
    public void testFinalizeResourcesLogging() {
        mockStatic(AppCenterLog.class);
        Closeable brokenResource = new Closeable() {
            @Override
            public void close() throws IOException {
                throw new IOException();
            }
        };
        mFileUtils.finalizeResources(Arrays.asList(brokenResource), "log me");
        verifyStatic(VerificationModeFactory.times(1));
    }
}