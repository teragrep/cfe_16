FROM rockylinux:9
COPY rpm/target/rpm/com.teragrep-cfe_16/RPMS/noarch/com.teragrep-cfe_16-*.rpm /rpm/
RUN yum -y localinstall /rpm/*.rpm && yum clean all

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
USER srv-cfe_16
ENTRYPOINT /entrypoint.sh
