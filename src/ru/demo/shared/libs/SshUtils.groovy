package ru.demo.shared.libs

import jenkins.plugins.publish_over_ssh.BapSshHostConfiguration
import jenkins.model.Jenkins
import com.jcraft.jsch.*
import java.io.*

class SshUtils {
    private static Jenkins inst
    private static String publishOverSshName
    private static String credentialsId
    private static String cmd

    static void init(Jenkins inst, String cmd) {
        this.inst = inst
        this.cmd = cmd
    }

    static ArrayList runCmd() {
        // если используются данные для подключения по SSH из плагина publish_over_ssh
        def pbSsh = inst.getDescriptor("jenkins.plugins.publish_over_ssh.BapSshPublisherPlugin")

        String sshUser = ''
        String sshPassword = ''
        String sshHost = ''
        def sshKeyPath = pbSsh.commonConfig.keyPath
        def sshPassphrase = pbSsh.commonConfig.passphrase
        def jenkinsHomePath = inst.root.getAbsolutePath()+'/'

        JSch jsch = new JSch()
        def session

        if (publishOverSshName != null) {
            pbSsh.hostConfigurations.each {
                if (it.name == publishOverSshName) {
                    sshUser = it.username
                    sshHost = it.hostname
                    sshPassword = it.getPassword()
                }
            }
        }

        // если доступ по SSH по логину и паролю из credential
        if (credentialsId != null) {
            def creds = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
                    com.cloudbees.plugins.credentials.common.StandardUsernameCredentials.class, inst, null, null ).find{
                it.id == credentialsId}
            sshUser = sshUser = creds.username
            sshPassword = (String)creds.password
            session = jsch.getSession(sshUser,sshHost, 22)
            session.setPassword(sshPassword)
        } else {
            jsch.addIdentity(jenkinsHomePath + sshKeyPath)
            //jsch.addIdentity(sshKeyPath, sshPassphrase)
            session = jsch.getSession(sshUser,sshHost, 22)
        }

        Properties prop = new Properties()
        prop.put("StrictHostKeyChecking", "no")
        session.setConfig(prop)

        session.connect()

        ChannelExec channel = (ChannelExec)session.openChannel("exec")
        channel.setCommand(cmd)
        channel.connect()

        InputStream is=channel.getInputStream()
        BufferedReader br = new BufferedReader(new InputStreamReader(is))
        ArrayList result = br.readLines()

        channel.disconnect()
        session.disconnect()

        return result
    }
}


