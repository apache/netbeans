/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cloud.amazon;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import java.io.IOException;
import java.util.List;
import org.netbeans.modules.cloud.amazon.serverplugin.AmazonJ2EEInstance;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient;
import com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription;
import com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityRequest;
import com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityResult;
import com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest;
import com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest;
import com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionResult;
import com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult;
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription;
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentStatus;
import com.amazonaws.services.elasticbeanstalk.model.S3Location;
import com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest;
import com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.cloud.common.spi.support.serverplugin.DeploymentStatus;
import org.netbeans.modules.cloud.common.spi.support.serverplugin.ProgressObjectImpl;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Describes single Amazon account.
 */
public class AmazonInstance {

    private static final RequestProcessor AMAZON_RP = new RequestProcessor("amazon aws", 1); // NOI18N
    
    private static final Logger LOG = Logger.getLogger(AmazonInstance.class.getSimpleName());
    
    public static final String DEFAULT_EMPTY_APPLICATION = "empty-nb-app-1.0.war";
    
    private final String keyId;
    private final String key;
    private final String name;
    private final String regionUrl;
    
    private ServerInstance serverInstance;

    public AmazonInstance(String name, String keyId, String key, String regionURL) {
        this.keyId = keyId;
        this.key = key;
        this.name = name;
        this.regionUrl = regionURL;
    }

    void setServerInstance(ServerInstance serverInstance) {
        this.serverInstance = serverInstance;
    }

    public ServerInstance getServerInstance() {
        return serverInstance;
    }
    
    public String getKeyId() {
        return keyId;
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }

    public String getRegionURL() {
        return regionUrl;
    }
    
    private AWSCredentials getCredentials() {
        return new BasicAWSCredentials(keyId, key);
    }
    
    private static AWSCredentials getCredentials(String keyId, String key) {
        return new BasicAWSCredentials(keyId, key);
    }
    
    public void testConnection() {
        assert !SwingUtilities.isEventDispatchThread();
        AWSElasticBeanstalk client = new AWSElasticBeanstalkClient(getCredentials());
        if (regionUrl != null) {
            client.setEndpoint(regionUrl);
        }
        client.createStorageLocation();
    }
    
    public List<AmazonJ2EEInstance> readJ2EEServerInstances() {
        assert !SwingUtilities.isEventDispatchThread();
        List<AmazonJ2EEInstance> res = new ArrayList<AmazonJ2EEInstance>();

//        try {
            LOG.log(Level.INFO, "read AWS environments"); // NOI18N
            AWSElasticBeanstalk client = new AWSElasticBeanstalkClient(getCredentials());
            if (regionUrl != null) {
                client.setEndpoint(regionUrl);
            }
            for (EnvironmentDescription ed : client.describeEnvironments().getEnvironments()) {
                AmazonJ2EEInstance inst = new AmazonJ2EEInstance(this, ed.getApplicationName(), 
                        ed.getEnvironmentName(), ed.getEnvironmentId(),
                        ed.getSolutionStackName());
                inst.updateState(ed.getStatus());
                res.add(inst);
            }
            LOG.log(Level.INFO, "environments available: "+res); // NOI18N
//        } catch (AmazonClientException ex) {
//            Exceptions.printStackTrace(ex);
//        }
        
// fake it:
//        res.add(new AmazonJ2EEInstance(this, "app", "env"));
            
        return res;
    }

    public List<String> readApplicationNames() {
        assert !SwingUtilities.isEventDispatchThread();
        List<String> res = new ArrayList<String>();

        try {
            LOG.log(Level.INFO, "read AWS applications"); // NOI18N
            AWSElasticBeanstalk client = new AWSElasticBeanstalkClient(getCredentials());
            if (regionUrl != null) {
                client.setEndpoint(regionUrl);
            }
            for (ApplicationDescription ad : client.describeApplications().getApplications()) {
                res.add(ad.getApplicationName());
            }
            LOG.log(Level.INFO, "applications available: "+res); // NOI18N
        } catch (AmazonClientException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return res;
    }

    /**
     * @return Map<application name, List<template name>>
     */
    public Map<String, List<String>> readApplicationTemplates() {
        assert !SwingUtilities.isEventDispatchThread();
        Map<String, List<String>> res = new HashMap<String, List<String>>();

        try {
            LOG.log(Level.INFO, "read AWS application templates"); // NOI18N
            AWSElasticBeanstalk client = new AWSElasticBeanstalkClient(getCredentials());
            if (regionUrl != null) {
                client.setEndpoint(regionUrl);
            }
            for (ApplicationDescription ad : client.describeApplications().getApplications()) {
                res.put(ad.getApplicationName(), ad.getConfigurationTemplates());
            }
            LOG.log(Level.INFO, "applications templates available: "+res); // NOI18N
        } catch (AmazonClientException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return res;
    }
    
    public List<String> readContainerTypes() {
        assert !SwingUtilities.isEventDispatchThread();
        List<String> res = new ArrayList<String>();

        try {
            LOG.log(Level.INFO, "read AWS solution stacks"); // NOI18N
            AWSElasticBeanstalk client = new AWSElasticBeanstalkClient(getCredentials());
            if (regionUrl != null) {
                client.setEndpoint(regionUrl);
            }
            res = client.listAvailableSolutionStacks().getSolutionStacks();
            LOG.log(Level.INFO, "solution stacks available: "+res); // NOI18N
        } catch (AmazonClientException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return res;
    }
    
    public boolean checkURLValidity(String url) {
        assert !SwingUtilities.isEventDispatchThread();
        AWSElasticBeanstalk client = new AWSElasticBeanstalkClient(getCredentials());
        if (regionUrl != null) {
            client.setEndpoint(regionUrl);
        }
        CheckDNSAvailabilityResult res = client.checkDNSAvailability(new CheckDNSAvailabilityRequest(url));
        return res.isAvailable();
    }

    public void createApplication(String appName) {
        assert !SwingUtilities.isEventDispatchThread();
        AWSElasticBeanstalk client = new AWSElasticBeanstalkClient(getCredentials());
        if (regionUrl != null) {
            client.setEndpoint(regionUrl);
        }
        CreateApplicationRequest req = new CreateApplicationRequest(appName);
        client.createApplication(req).getApplication();
    }
    
    public S3Location createDefaultEmptyApplication() {
        assert !SwingUtilities.isEventDispatchThread();
        AWSElasticBeanstalk client = new AWSElasticBeanstalkClient(getCredentials());
        if (regionUrl != null) {
            client.setEndpoint(regionUrl);
        }
        AmazonS3 s3 = new AmazonS3Client(getCredentials(keyId, key));
        
        String bucket = client.createStorageLocation().getS3Bucket();
        boolean exist = false;
        try {
            s3.getObjectMetadata(bucket, DEFAULT_EMPTY_APPLICATION);
            exist = true;
        } catch (AmazonS3Exception ex) {
            // this mean object does not exist in S3 - fine
        }
        if (!exist) {
            InputStream is = AmazonInstance.class.getResourceAsStream("resources/empty.war");
            s3.putObject(new PutObjectRequest(bucket, DEFAULT_EMPTY_APPLICATION, is, new ObjectMetadata()));
            try {
                is.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return new S3Location().withS3Bucket(bucket).withS3Key(DEFAULT_EMPTY_APPLICATION);
    }
    
    public void createInitialEmptyApplication(String appName) {
        assert !SwingUtilities.isEventDispatchThread();
        AWSElasticBeanstalk client = new AWSElasticBeanstalkClient(getCredentials());
        if (regionUrl != null) {
            client.setEndpoint(regionUrl);
        }
        S3Location slocation = createDefaultEmptyApplication();
        
        CreateApplicationVersionRequest req = new CreateApplicationVersionRequest(appName, "blank application from NetBeans").
                withSourceBundle(slocation);
        client.createApplicationVersion(req);
    }

    public String createEnvironment(String appName, String envName, String url, String containerType, String template) {
        assert !SwingUtilities.isEventDispatchThread();
        AWSElasticBeanstalk client = new AWSElasticBeanstalkClient(getCredentials());
        if (regionUrl != null) {
            client.setEndpoint(regionUrl);
        }
        CreateEnvironmentRequest req = new CreateEnvironmentRequest(appName, envName).
                withCNAMEPrefix(url).
                withSolutionStackName(containerType);
        if (template != null) {
            req = req.withTemplateName(template);
        }
        return client.createEnvironment(req).getEnvironmentId();
    }

    public static Future<DeploymentStatus> deployAsync(final File f, final String applicationName, 
                         final String environmentId, final String keyId, final String key,
                         final ProgressObjectImpl po, final String regionUrl) {
        return runAsynchronously(new Callable<DeploymentStatus>() {
            @Override
            public DeploymentStatus call() throws Exception {
                String url[] = new String[1];
                DeploymentStatus ds = deploy(f, applicationName, environmentId, keyId, key, po, url, regionUrl);
                LOG.log(Level.INFO, "deployment result: "+ds); // NOI18N
                po.updateDepoymentResult(ds, url[0]);
                return ds;
            }
        });
    }
    
    public static DeploymentStatus deploy(File f, String applicationName, 
                          String environmentId, String keyId, String key,
                          ProgressObjectImpl po, String[] url, String regionUrl) {
        assert !SwingUtilities.isEventDispatchThread();
        try {
            if (po != null) {
                try {
                po.updateDepoymentStage(NbBundle.getMessage(AmazonInstance.class, "MSG_DEPLOY_AUTH"));
                } catch (Throwable tt) {
                    tt.printStackTrace();
                }
            }
            LOG.log(Level.INFO, "deploy to AWS["+environmentId+"] "+f); // NOI18N
            AWSElasticBeanstalk client = new AWSElasticBeanstalkClient(getCredentials(keyId, key));
            if (regionUrl != null) {
                client.setEndpoint(regionUrl);
            }
            AmazonS3 s3 = new AmazonS3Client(getCredentials(keyId, key));

            if (po != null) {
                po.updateDepoymentStage(NbBundle.getMessage(AmazonInstance.class, "MSG_DEPLOY_UPLOAD"));
            }
            String label = f.getName().toLowerCase();
            assert label.endsWith(".war") || label.endsWith(".ear") || label.endsWith(".jar") : "war/jar/ear archive expected: "+f; // NOI18N
            String version = new SimpleDateFormat("yyyyMMdd-HHmmss-SSSS").format(new Date()); // NOI18N
            label = label.substring(0, label.length()-4) + "-" + version + "." + label.substring(label.length()-3); // NOI18N

            String bucket = client.createStorageLocation().getS3Bucket();
            LOG.log(Level.INFO, "using bucket "+bucket); // NOI18N
            LOG.log(Level.INFO, "label "+label); // NOI18N
            s3.putObject(new PutObjectRequest(bucket, label, f));
            S3Location slocation = new S3Location().withS3Bucket(bucket).withS3Key(label);

            if (po != null) {
                po.updateDepoymentStage(NbBundle.getMessage(AmazonInstance.class, "MSG_DEPLOY_UPDATE"));
            }
            CreateApplicationVersionRequest newApp = new CreateApplicationVersionRequest().
                    withVersionLabel(label).
                    withApplicationName(applicationName).
                    withDescription(NbBundle.getMessage(AmazonInstance.class, "MSG_DEPLOY_NB")).
                    withAutoCreateApplication(Boolean.FALSE).
                    withSourceBundle(slocation);
            client.createApplicationVersion(newApp);

            UpdateEnvironmentRequest updateReq = new UpdateEnvironmentRequest().
                    withEnvironmentId(environmentId).
                    withVersionLabel(label);
            try {
                UpdateEnvironmentResult result = client.updateEnvironment(updateReq);
                url[0] = "http://"+result.getEndpointURL(); // NOI18N
                LOG.log(Level.INFO, "environment updated "+result); // NOI18N
            } catch (AmazonServiceException as) {
                LOG.log(Level.INFO, "environment update failed", as); // NOI18N
                if (po != null) {
                    po.updateDepoymentStage(as.toString());
                }
                return DeploymentStatus.FAILED;
            }

            if (po != null) {
                po.updateDepoymentStage(NbBundle.getMessage(AmazonInstance.class, "MSG_DEPLOY_REDEPLOY"));
            }
            DescribeEnvironmentsRequest request = new DescribeEnvironmentsRequest().withEnvironmentIds(environmentId);
            while (true) {
                try {
                    // let's wait
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                DescribeEnvironmentsResult envir = client.describeEnvironments(request);
                if (envir.getEnvironments().size() != 1) {
                    // something wrong - environment was deleted??
                    return DeploymentStatus.UNKNOWN;
                }
                EnvironmentDescription env = envir.getEnvironments().get(0);
                LOG.log(Level.INFO, "AWS["+environmentId+"] status: "+env.getStatus() +" and health:"+ env.getHealth()); // NOI18N
                if (EnvironmentStatus.fromValue(env.getStatus()) == EnvironmentStatus.Ready) {
                    return DeploymentStatus.SUCCESS;
                }
                if (EnvironmentStatus.fromValue(env.getStatus()) == EnvironmentStatus.Terminated ||
                        EnvironmentStatus.fromValue(env.getStatus()) == EnvironmentStatus.Terminating) {
                    return DeploymentStatus.UNKNOWN;
                }
            }
        } catch (AmazonClientException ex) {
            Exceptions.printStackTrace(ex);
            return DeploymentStatus.UNKNOWN;
        }
    }

    public static <T> Future<T> runAsynchronously(Callable<T> callable) {
        return runAsynchronously(callable, null);
    }
    
    public static synchronized <T> Future<T> runAsynchronously(Callable<T> callable, AmazonInstance ai) {
        Future<T> f = AMAZON_RP.submit(callable);
        return f;
    }
    
}
