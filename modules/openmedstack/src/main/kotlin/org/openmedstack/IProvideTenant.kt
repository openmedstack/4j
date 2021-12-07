package org.openmedstack

interface IProvideTenant {
    val tenantName: String?
}

class ConfigurationTenantProvider constructor(private val _deploymentConfiguration: DeploymentConfiguration) :
    IProvideTenant {
    override val tenantName: String? = _deploymentConfiguration.tenantId
}
