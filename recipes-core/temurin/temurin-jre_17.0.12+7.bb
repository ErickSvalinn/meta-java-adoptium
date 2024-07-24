DESCRIPTION               = "Temurin JRE Binaries"
HOMEPAGE                  = "https://adoptium.net"
LICENSE                   = "GPL-2.0-with-classpath-exception"
LIC_FILES_CHKSUM          = "file://NOTICE;md5=0118abc27c1406bf05d7617cc4db30c9"
FILESEXTRAPATHS_prepend   = "${THISDIR}/files:"
S                         = "${WORKDIR}/jdk-${PV}-jre"
DEPENDS                   = "patchelf-native"

# Dependencies
RDEPENDS:${PN}  = " \
  alsa-lib \
  freetype \
  glibc \
  libx11 \
  libxext \
  libxi \
  libxrender \
  libxtst \
  zlib \
"

# Binaries are linked with glibc
COMPATIBLE_HOST:libc-musl = "null"

# Defines java variables
PV_MAJOR    = "${@d.getVar('PV').split('.')[0]}"
PV_UNDER    = "${@d.getVar('PV').replace('+', '_')}"
JAVA_ARCH   = "arm"
JAVA_HOME   = "${libdir}/jvm/openjdk-${PV_MAJOR}-jre"

# Convert PV to forms needed to download the tarball
SRC_URI            = "file://OpenJDK${PV_MAJOR}U-jre_${JAVA_ARCH}_linux_hotspot_${PV_UNDER}.tar.gz"
SRC_URI[sha256sum] = "f093094abe0cb2bb5a255d8180810030321073520541f289926c4682eda76136"

# Prevent the packaging task from stripping out
# debugging symbols, since there are none.
INHIBIT_DEFAULT_DEPS        = "1"
INHIBIT_PACKAGE_STRIP       = "1"
INHIBIT_SYSROOT_STRIP       = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
EXCLUDE_FROM_SHLIBS         = "1"

# Package unversioned libraries
SOLIBS = ".so"
FILES_SOLIBSDEV = ""

# Ignore "doesn't have GNU_HASH (didn't pass LDFLAGS?)" errors
INSANE_SKIP:${PN} = "ldflags"

# Ignore QA Issue: non -dev/-dbg/nativesdk- package
INSANE_SKIP:${PN}:append = " dev-so"

# Disable stuff not needed for packaging binaries
do_configure[noexec]        = "1"
do_compile[noexec]          = "1"

# Install the binaries
do_install() {
  # Create the opt folder into the final image, ${D} is ${WORKDIR}/image
  install -d ${D}${JAVA_HOME}

  # Copy sources to the final image
  # -R, -r, --recursive       copy directories recursively
  # -P, --no-dereference      never follow symbolic links in SOURCE
  # --preserve[=ATTR_LIST]    preserve the specified attributes
  # -v, --verbose             explain what is being done
  #cp -r ${S}/* ${D}${JAVA_HOME}
  cp --recursive --no-dereference --preserve=mode,links ${S}/* ${D}${JAVA_HOME}

  # Set the interpreter
  LDLINUX=$(basename $(ls -1 ${RECIPE_SYSROOT}${base_libdir}/ld-linux* | sort | head -n1))
  if [ -n "$LDLINUX" ]; then
    for i in ${D}${JAVA_HOME}/bin/* ; do
      patchelf --set-interpreter ${base_libdir}/$LDLINUX $i
    done
  fi
}

# Provides 'temurin-jre'
RPROVIDES:${PN} = "${BPN}"

# Append files
FILES:${PN} = "${JAVA_HOME}"

# Alternatives lower than corresponding JDK
inherit update-alternatives
ALTERNATIVE_PRIORITY           = "90"
ALTERNATIVE:${PN}              = "java keytool"
ALTERNATIVE_LINK_NAME[java]    = "${bindir}/java"
ALTERNATIVE_TARGET[java]       = "${JAVA_HOME}/bin/java"
ALTERNATIVE_LINK_NAME[keytool] = "${bindir}/keytool"
ALTERNATIVE_TARGET[keytool]    = "${JAVA_HOME}/bin/keytool"
