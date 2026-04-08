FROM rockylinux/rockylinux:9
RUN yum -y localinstall /rpm/*.rpm && yum clean all

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
USER srv-cfe_16
ENTRYPOINT /entrypoint.sh
