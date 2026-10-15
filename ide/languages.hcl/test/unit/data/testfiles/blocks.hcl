terraform {
  required_providers {}
}

variable name {}

data "aws_subnets" "private" {
  filter {
    name   = "vpc-id"
    values = [local.vpc_id]
  }

  tags = {
    Reach = "private"
  }
}
