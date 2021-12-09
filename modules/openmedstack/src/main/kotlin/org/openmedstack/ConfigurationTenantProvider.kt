package org.openmedstack

class ConfigurationTenantProvider constructor(_deploymentConfiguration: DeploymentConfiguration) :
    IProvideTenant {
    override val tenantName: String? = _deploymentConfiguration.tenantId
}