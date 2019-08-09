<template>
  <div class="app-container">
    <el-form :inline="true" :model="searchFormData" class="demo-form-inline" size="mini">
      <el-form-item label="serviceId">
        <el-input v-model="searchFormData.serviceId" :clearable="true" placeholder="serviceId" style="width: 250px;" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="onSearchTable">查询</el-button>
      </el-form-item>
    </el-form>
    <el-table
      :data="tableData"
      style="width: 100%;margin-bottom: 20px;"
      border
      row-key="id"
    >
      <el-table-column
        prop="serviceId"
        label="服务名称"
        width="200"
      >
        <template slot-scope="scope">
          <span v-html="renderServiceName(scope.row)"></span>
        </template>
      </el-table-column>
      <el-table-column
        prop="ipPort"
        label="IP端口"
        width="250"
      />
      <el-table-column
        prop="status"
        label="服务状态"
        width="100"
      >
        <template slot-scope="scope">
          <el-tag v-if="scope.row.parentId > 0 && scope.row.status === 'UP'" type="success">已上线</el-tag>
          <el-tag v-if="scope.row.parentId > 0 && scope.row.status === 'STARTING'" type="info">正在启动</el-tag>
          <el-tag v-if="scope.row.parentId > 0 && scope.row.status === 'UNKNOWN'">未知</el-tag>
          <el-tag v-if="scope.row.parentId > 0 && (scope.row.status === 'OUT_OF_SERVICE' || scope.row.status === 'DOWN')" type="danger">已下线</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        prop="updateTime"
        label="最后更新时间"
        width="160"
      />
      <el-table-column
        label="操作"
        width="100"
      >
        <template slot-scope="scope">
          <el-button v-if="scope.row.parentId > 0 && scope.row.status === 'UP'" type="text" size="mini" @click="onOffline(scope.row)">下线</el-button>
          <el-button v-if="scope.row.parentId > 0 && scope.row.status === 'OUT_OF_SERVICE'" type="text" size="mini" @click="onOnline(scope.row)">上线</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  data() {
    return {
      searchFormData: {
        serviceId: ''
      },
      tableData: []
    }
  },
  created() {
    this.loadTable()
  },
  methods: {
    loadTable: function() {
      this.post('service.instance.list', this.searchFormData, function(resp) {
        this.tableData = this.buildTreeData(resp.data)
      })
    },
    buildTreeData: function(data) {
      data.forEach(ele => {
        const parentId = ele.parentId
        if (parentId === 0) {
          // 是根元素 ,不做任何操作,如果是正常的for-i循环,可以直接continue.
        } else {
          // 如果ele是子元素的话 ,把ele扔到他的父亲的child数组中.
          data.forEach(d => {
            if (d.id === parentId) {
              let childArray = d.children
              if (!childArray) {
                childArray = []
              }
              childArray.push(ele)
              d.children = childArray
            }
          })
        }
      })
      // 去除重复元素
      data = data.filter(ele => ele.parentId === 0)
      return data
    },
    onSearchTable: function() {
      this.loadTable()
    },
    onOffline: function(row) {
      this.confirm('确定要下线【' + row.serviceId + '】吗?', function(done) {
        this.post('service.instance.offline', row, function() {
          this.tip('下线成功')
          done()
        })
      })
    },
    onOnline: function(row) {
      this.confirm('确定要上线【' + row.serviceId + '】吗?', function(done) {
        this.post('service.instance.online', row, function() {
          this.tip('上线成功')
          done()
        })
      })
    },
    renderServiceName: function(row) {
      let instanceCount = ''
      if (row.children && row.children.length > 0) {
        const onlineCount = row.children.filter(el => {
          return el.status === 'UP'
        }).length
        instanceCount = ` (${onlineCount}/${row.children.length})`
      }
      return row.serviceId + instanceCount
    }
  }
}
</script>
