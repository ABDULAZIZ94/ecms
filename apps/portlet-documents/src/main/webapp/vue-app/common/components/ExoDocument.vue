<template>
  <v-list-item @click.prevent="openPreview()">
    <v-list-item-icon class="mx-0">
      <v-icon :color="documentIcon.color" x-large>
        {{ documentIcon.ico }}
      </v-icon>
    </v-list-item-icon>
    <v-list-item-content>
      <v-list-item-title :title="decoder(document.title)" v-html="document.title"/>
      <v-list-item-subtitle>
        <div :title="absoluteDateModified()" class="color-title">
          {{ relativeDateModified }}
          <v-icon color="#a8b3c5">
            mdi-menu-right
          </v-icon>
          {{ document.drive }}
        </div>
      </v-list-item-subtitle>
    </v-list-item-content>
  </v-list-item>
</template>
<script>
  export default {
    props: {
      document: {
        type: Object,
        default: () => null,
      }
    },
    computed: {
      documentIcon() {
        const icon = {}
        if (this.document.fileType.includes('pdf')) {
          icon.ico = 'mdi-file-pdf';
          icon.color = '#d07b7b';
        } else if (this.document.fileType.includes('presentation') || this.document.fileType.includes('powerpoint')) {
          icon.ico = 'mdi-file-powerpoint';
          icon.color = '#e45030';
        } else if (this.document.fileType.includes('sheet') || this.document.fileType.includes('excel')) {
          icon.ico = 'mdi-file-excel';
          icon.color = '#1a744b';
        } else if (this.document.fileType.includes('word') || this.document.fileType.includes('opendocument') || this.document.fileType.includes('rtf') ) {
          icon.ico = 'mdi-file-word';
          icon.color = '#094d7f';
        } else if (this.document.fileType.includes('plain')) {
          icon.ico = 'mdi-clipboard-text';
          icon.color = '#1c9bd7';
        } else if (this.document.fileType.includes('image')) {
          icon.ico = 'mdi-image';
          icon.color = '#eab320';
        } else {
          icon.ico = 'mdi-file';
          icon.color = '#cdcccc';
        }
        return icon;
      },
      relativeDateModified() {
        return this.getRelativeTime(this.document.date);
      }
    },
    methods: {
      getRelativeTime(previous) {
        const msPerMinute = 60 * 1000;
        const msPerHour = msPerMinute * 60;
        const msPerDay = msPerHour * 24;
        const msPerMaxDays = msPerDay * 2;
        const elapsed = new Date().getTime() - previous;
        if (elapsed < msPerMinute) {
          return this.$t('documents.timeConvert.Less_Than_A_Minute');
        } else if (elapsed === msPerMinute) {
          return this.$t('documents.timeConvert.About_A_Minute');
        } else if (elapsed < msPerHour) {
          return this.$t('documents.timeConvert.About_?_Minutes').replace('{0}', Math.round(elapsed / msPerMinute));
        } else if (elapsed === msPerHour) {
          return this.$t('documents.timeConvert.About_An_Hour');
        } else if (elapsed < msPerDay) {
          return this.$t('documents.timeConvert.About_?_Hours').replace('{0}', Math.round(elapsed / msPerHour));
        } else if (elapsed === msPerDay) {
          return this.$t('documents.timeConvert.About_A_Day');
        } else if (elapsed < msPerMaxDays) {
          return this.$t('documents.timeConvert.About_?_Days').replace('{0}', Math.round(elapsed / msPerDay));
        } else {
          return this.absoluteDateModified({dateStyle: "short"});
        }
      },
      absoluteDateModified(options) {
        const lang = eXo && eXo.env && eXo.env.portal && eXo.env.portal.language || 'en';
        return new Date(this.document.date).toLocaleString(lang, options).split("/").join("-");
      },
      fileInfo() {
        return `${this.$t("documents.preview.updatedOn")} ${this.absoluteDateModified()} ${this.$t("documents.preview.updatedBy")} ${this.document.lastEditor} ${this.document.size}`;
      },
      decoder(str) {
        const text = document.createElement('textarea');
        text.innerHTML = str;
        return text.value;
      },
      openPreview() {
        documentPreview.init({
          doc: {
            id: this.document.id,
            repository: 'repository',
            workspace: 'collaboration',
            path: this.document.nodePath || this.document.path,
            title: this.document.title,
            downloadUrl: this.document.downloadUrl,
            openUrl: this.document.url || this.document.openUrl,
            breadCrumb: this.document.previewBreadcrumb,
            fileInfo: this.fileInfo()
          },
          version: {                                                                 
            number : this.document.version 
          },
          showComments: false
        });
      }
    }
  }
</script>