package com.github.sgwhp.openapm.agent;

import com.github.sgwhp.openapm.agent.util.Log;
import com.github.sgwhp.openapm.agent.visitor.*;

import java.util.*;
import java.net.*;
import java.io.*;
/**
 * Created by user on 2016/8/3.
 */

/*
public class Proguard {

    private static final String NR_PROPERTIES = "newrelic.properties";
    private static final String PROP_NR_APP_TOKEN = "com.newrelic.application_token";
    private static final String PROP_UPLOADING_ENABLED = "com.newrelic.enable_proguard_upload";
    private static final String PROP_MAPPING_API_HOST = "com.newrelic.mapping_upload_host";
    private static final String MAPPING_FILENAME = "mapping.txt";
    private static final String DEFAULT_MAPPING_API_HOST = "mobile-symbol-upload.newrelic.com";
    private static final String MAPPING_API_PATH = "/symbol";
    private static final String LICENSE_KEY_HEADER = "X-APP-LICENSE-KEY";
    private final Log log;
    private String projectRoot;
    private String licenseKey;
    private boolean uploadingEnabled;
    private String mappingApiHost;

    public Proguard(final Log log) {
        this.licenseKey = null;
        this.uploadingEnabled = true;
        this.mappingApiHost = null;
        this.log = log;
    }


    public void findAndSendMapFile() {
        String mappingString = "";
        if (this.getProjectRoot() != null) {
            if (!this.fetchConfiguration()) {
                return;
            }

            final File projectRoot = new File(this.getProjectRoot());
            final IOFileFilter fileFilter = FileFilterUtils.nameFileFilter("mapping.txt");
            final Collection<File> files = FileUtils.listFiles(projectRoot, fileFilter, TrueFileFilter.INSTANCE);
            if (files.isEmpty()) {
                this.log.e("While evidence of ProGuard was detected, New Relic failed to find your mapping.txt file.");
                this.log.e("To de-obfuscate your builds, you'll need to upload your mapping.txt manually.");
            }
            for (final File file : files) {
                this.log.d("Found mapping.txt: " + file.getPath());
                try {
                    final FileWriter fileWriter = new FileWriter(file, true);
                    fileWriter.write("# NR_BUILD_ID -> " + OpenapmClassVisitor.getBuildId());
                    fileWriter.close();
                    mappingString += FileUtils.readFileToString(file);
                }
                catch (FileNotFoundException e) {
                    this.log.e("Unable to open your mapping.txt file: " + e.getLocalizedMessage());
                    this.log.e("To de-obfuscate your builds, you'll need to upload your mapping.txt manually.");
                }
                catch (IOException e2) {
                    this.log.e("Unable to open your mapping.txt file: " + e2.getLocalizedMessage());
                    this.log.e("To de-obfuscate your builds, you'll need to upload your mapping.txt manually.");
                }
            }
            if (this.uploadingEnabled) {
                this.sendMapping(mappingString);
            }
        }
    }

    private String getProjectRoot() {
        if (this.projectRoot == null) {
            final String encodedProjectRoot = RewriterAgent.getAgentOptions().get("projectRoot");
            if (encodedProjectRoot == null) {
                this.log.d("Unable to determine project root, falling back to CWD.");
                this.projectRoot = System.getProperty("user.dir");
            }
            else {
                this.projectRoot = new String(BaseEncoding.base64().decode(encodedProjectRoot));
                this.log.d("Project root: " + this.projectRoot);
            }
        }
        return this.projectRoot;
    }

    private boolean fetchConfiguration() {
        try {
            final Reader propsReader = new BufferedReader(new FileReader(this.getProjectRoot() + File.separator + "newrelic.properties"));
            final Properties newRelicProps = new Properties();
            newRelicProps.load(propsReader);
            this.licenseKey = newRelicProps.getProperty("com.newrelic.application_token");
            this.uploadingEnabled = newRelicProps.getProperty("com.newrelic.enable_proguard_upload", "true").equals("true");
            this.mappingApiHost = newRelicProps.getProperty("com.newrelic.mapping_upload_host");
            if (this.licenseKey == null) {
                this.log.e("Unable to find a value for com.newrelic.application_token in your newrelic.properties");
                this.log.e("To de-obfuscate your builds, you'll need to upload your mapping.txt manually.");
                return false;
            }
            propsReader.close();
        }
        catch (FileNotFoundException e) {
            this.log.e("Unable to find your newrelic.properties in the project root (" + this.getProjectRoot() + "): " + e.getLocalizedMessage());
            this.log.e("To de-obfuscate your builds, you'll need to upload your mapping.txt manually.");
            return false;
        }
        catch (IOException e2) {
            this.log.e("Unable to read your newrelic.properties in the project root (" + this.getProjectRoot() + "): " + e2.getLocalizedMessage());
            this.log.e("To de-obfuscate your builds, you'll need to upload your mapping.txt manually.");
            return false;
        }
        return true;
    }

    private void sendMapping(final String mapping) {
        final StringBuilder requestBody = new StringBuilder();
        requestBody.append("proguard=" + URLEncoder.encode(mapping));
        requestBody.append("&buildId=" + OpenapmClassVisitor.getBuildId());
        try {
            String host = "mobile-symbol-upload.newrelic.com";
            if (this.mappingApiHost != null) {
                host = this.mappingApiHost;
            }
            final URL url = new URL("https://" + host + "/symbol");
            final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("X-APP-LICENSE-KEY", this.licenseKey);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", Integer.toString(requestBody.length()));
            final DataOutputStream request = new DataOutputStream(connection.getOutputStream());
            request.writeBytes(requestBody.toString());
            request.close();
            final int responseCode = connection.getResponseCode();
            if (responseCode == 400) {
                final InputStream inputStream = connection.getErrorStream();
                final String response = convertStreamToString(inputStream);
                this.log.e("Unable to send your ProGuard mapping.txt to New Relic as the params are incorrect: " + response);
                this.log.e("To de-obfuscate your builds, you'll need to upload your mapping.txt manually.");
            }
            else if (responseCode > 400) {
                final InputStream inputStream = connection.getErrorStream();
                final String response = convertStreamToString(inputStream);
                this.log.e("Unable to send your ProGuard mapping.txt to New Relic - received status " + responseCode + ": " + response);
                this.log.e("To de-obfuscate your builds, you'll need to upload your mapping.txt manually.");
            }
            this.log.d("Successfully sent mapping.txt to New Relic.");
            connection.disconnect();
        }
        catch (IOException e) {
            this.log.e("Encountered an error while uploading your ProGuard mapping to New Relic", e);
            this.log.e("To de-obfuscate your builds, you'll need to upload your mapping.txt manually.");
        }
    }


    private static String convertStreamToString(final InputStream is) {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        final StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            try {
                is.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        finally {
            try {
                is.close();
            }
            catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        return sb.toString();
    }


}
*/