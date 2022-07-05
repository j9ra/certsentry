<template>
    <v-card elevation="5" class="pa-2">
        <v-card-title class="overline">{{titlename}}</v-card-title>
        <v-data-table
            :headers="headers"
            :items="items"
            hide-default-footer
            item-key="territory"
            >

            <template v-slot:item.nextUpdate="{ item }">
              <span>{{ item.nextUpdate | formatDate}}</span>
            </template>

            <template v-slot:item.lastCheck="{ item }">
              <span>{{ item.lastCheck | formatDate}}</span>
            </template>

            <template v-slot:item.isValid="{ item }">
              <span>{{ (item.isValid == true)  ? 'Yes' : 'No' }}</span>
            </template>
    
            <template v-slot:no-data>
                <v-alert dense  type="error" border="right" colored-border elevation="2" class="mt-5" v-if="error">
                  Failed to load data: <strong>{{errors[0].message}}</strong>
                </v-alert>
               <span v-else>no data</span>
            </template>
      
        </v-data-table>
    </v-card>
</template>

<script>
import api from '@/components/http-common.js'
import moment from 'moment';

export default {
    data () {
      return {
        titlename: 'Trusted list expirations',
        enabled: null,
        items: [],
        headers: [
          { text: 'Territory', align: 'start', sortable: false, value: 'territory', },
          { text: 'List expire', value: 'nextUpdate', sortable: false, },
          { text: 'Last check', value: 'lastCheck', sortable: false, },
          { text: 'Valid', value: 'isValid', sortable: false, },
        ],
        error: false,
        errors: [],
      }
    },
    created() {
      this.initialize();
    },
    methods: {
      initialize () {
        api.getSecured(`/trustlists/expires`)
         .then(response => {
           this.loaded = false
           this.items = response.data;
           this.loaded = true
          })
          .catch(e => {
            console.log('/trustlists/expires: response error: ' + e);
            this.errors.push(e)
            this.error = true;
          })      
      },
  },
  filters: {
      formatDate(value) {
        if (value) {
          return moment(value).format('YYYY-MM-DD HH:mm')
        }
      }
    },

}
</script>