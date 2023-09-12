/*
This is a multi line commant
*/
locals {
  azs    = [ for i in ["a", "b", "c"] : "us-east-1${i}" ]
  vpc_id = data.terraform_remote_state.vpc.outputs.vpc_id
  multiline = <<-EOT
    This is a
    multiline text
  EOT
}

variable "domain_name" {
  type    = string
  default = "example.com"
}


data "http" "example" {
  url = "https://${var.domain_name}/something"

  # Optional request headers
  request_headers = {
    Accept = "application/json"
  }
}

resource "tls_private_key" "example" {
  algorithm = "RSA"
  rsa_bits  = 4096
}

resource "tls_cert_request" "example" {
  private_key_pem = tls_private_key.example.private_key_pem

  is_ca_certificate = false

  subject {
    common_name  = var.domain_name
    organization = "ACME Examples, Inc"
  }
}