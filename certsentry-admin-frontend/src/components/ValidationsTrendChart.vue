<template>
  <v-card elevation="5" class="pa-5">
    <v-card-title class="overline">{{ titlename }}</v-card-title>
    
    <div>
      <line-chart v-if="loaded" :chart-data="chartdata" :chart-labels="chartlabels"></line-chart> 
    </div>

    <v-alert dense  type="error" border="right" colored-border elevation="2" v-if="error">
      Failed to load data: <strong>{{errors[0].message}}</strong>
    </v-alert>

  </v-card>
</template>

<script>
  import LineChart from './LineChart.js'
  import api from '@/components/http-common.js'

  export default {
    components: {
      LineChart
    },
    data: () => ({
      titlename: 'Validation requests trend',
      loaded: false,
      chartdata: null,
      chartlabels: null,
      error: false,
      errors: [],
    }),
    mounted () {
      this.initialize();
    },
    methods: {
    initialize () {
        api.getSecured(`/system/validationTrends`)
         .then(response => {

          this.loaded = false
          this.chartdata = response.data.values
          this.chartlabels = response.data.labels
          this.loaded = true

          })
          .catch(e => {
            console.log("/system/validationTrends: response error: " + e)
            this.error = true;
            this.errors.push(e)
          })      
      },
  }
      
    
  }
</script>

