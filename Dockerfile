FROM rockylinux:9
COPY target/rpm/com.teragrep-cfe_16/RPMS/noarch/com.teragrep-cfe_16-*.rpm /rpm/
RUN yum -y localinstall /rpm/*.rpm && yum clean all

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT /entrypoint.sh
